package xuyihao.zk.client.core;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xuyihao.zk.client.core.common.Operations;

import java.util.List;

/**
 * Created by xuyh at 2017/11/30 14:40.
 * <p>
 * <pre>
 * 子节点监听方式
 *
 * 实现思路：监听子节点状态
 * 1.在父节点(持久化)下创建临时节点，实际创建的节点路径会根据数量进行自增(ZK自编号方式创建节点)。
 * 2.创建节点成功后，首先获取父节点下的子节点列表，判断本线程的路径后缀编号是否是所有子节点中最小的，若是则成为leader，反之监听本节点前一个节点(路径排序为本节点路径数字减一的节点)变动状态(通过getData()方法注册watcher)
 * 3.当监听对象状态变动(节点删除状态)后watcher会接收到通知，这时再次判断父节点下的子节点的排序状态，若满足本线程的路径后缀编号最小则成为leader，反之继续注册watcher监听前一个节点状态
 * </pre>
 */
public class Leader {
	private static Logger logger = LoggerFactory.getLogger(Leader.class);
	private final static String BASE_NODE_PATH = "/Leader";
	private final static String CHILD_NODE_PATH = "host_process_no_";
	private String finalNodePath;
	private String host = "127.0.0.1";
	private String port = "2181";
	private ZooKeeper zooKeeper;
	private PreviousNodeWatcher previousNodeWatcher;

	private boolean registered = false;//是否成功注册子节点标志位(是否成功注册领导竞争)
	private boolean connected = false;// 是否连接成功标志位
	private boolean leader = false;// 是否是主节点标志位

	/**
	 * 新建主节点选举对象,阻塞最多30秒
	 *
	 * @param host zk主机地址
	 * @param port zk服务端口
	 */
	public Leader(String host, String port) {
		this.host = host;
		this.port = port;
		this.previousNodeWatcher = new PreviousNodeWatcher(this);
		registered = this.connectRegister();
	}

	/**
	 * 销毁本主节点选举对象
	 *
	 * @return
	 */
	public boolean destroy() {
		return this.disconnectZooKeeper();
	}

	/**
	 * 本主节点选举对象是否获取主节点地位
	 *
	 * @return
	 */
	public boolean isLeader() {
		return this.leader;
	}

	/**
	 * 本主节点选举对象是否与ZOOKEEPER连接成功
	 *
	 * @return
	 */
	public boolean isConnected() {
		return this.connected;
	}

	/**
	 * 本主节点选举对象是否成功在ZOOKEEPER中注册
	 *
	 * @return
	 */
	public boolean isRegistered() {
		return this.registered;
	}

	/**
	 * <pre>
	 * 如果阻塞30秒时间内没有连接成功，Leader将不创建子节点，finalNodePath将为null(即本Leader对象失效)，
	 * 30秒之后即使连接成功connected被置为true(Zookeeper异步连接成功又将connected置为true)了，Leader的连接也视为失败
	 * 这时候就需要一个标志位来说明这个Leader虽然连接ZK成功，但没有注册子节点，也是无效的leader竞争
	 * </pre>
	 * <p>
	 * {@link xuyihao.zk.client.core.Leader#registered}
	 *
	 * @return 是否注册成功
	 */
	private boolean connectRegister() {
		try {
			zooKeeper = new ZooKeeper(host + ":" + port, 60000, event -> {
				if (event.getState() == Watcher.Event.KeeperState.AuthFailed) {
					connected = false;
					leader = false;
				} else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
					connected = false;
					leader = false;
				} else if (event.getState() == Watcher.Event.KeeperState.Expired) {
					connected = false;
					leader = false;
				} else {
					if (event.getType() == Watcher.Event.EventType.None) {// 说明连接成功了
						connected = true;
					}
				}
			});

			int i = 1;
			while (!connected) {// 等待异步连接成功,超过时间30s则退出等待
				if (i == 100)
					break;
				Thread.sleep(300);
				i++;
			}

			if (connected) {
				// 创建父节点
				if (zooKeeper.exists(BASE_NODE_PATH, false) == null) {
					zooKeeper.create(BASE_NODE_PATH, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}
				// 创建子节点
				finalNodePath = zooKeeper.create(BASE_NODE_PATH + "/" + CHILD_NODE_PATH, "".getBytes(),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

				// 检查一次是否是主节点
				checkLeader();
				return true;
			} else {
				logger.warn("Connect zookeeper failed. Time consumes 30 s");
				return false;
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			return false;
		}
	}

	private boolean disconnectZooKeeper() {
		if (zooKeeper == null)
			return false;
		try {
			zooKeeper.close();
			connected = false;
			leader = false;
		} catch (Exception e) {
			logger.warn(String.format("ZK disconnect failed. [%s]", e.getMessage()), e);
		}
		return true;
	}

	private void checkLeader() {
		if (!connected)
			return;
		try {
			// 获取子节点列表，若没有成为leader，注册监听，监听对象应当是比本节点路径编号小一(或者排在前面一位)的节点
			List<String> childrenList = zooKeeper.getChildren(BASE_NODE_PATH, false);

			if (Operations.judgePathNumMin(BASE_NODE_PATH, CHILD_NODE_PATH, finalNodePath, childrenList)) {
				leader = true;// 成为leader
			} else {
				watchPreviousNode(childrenList);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}

	private void watchPreviousNode(List<String> paths) {
		if (paths.isEmpty() || paths.size() == 1) {
			return;
		}
		int currentNodeIndex = paths
				.indexOf(finalNodePath.substring((BASE_NODE_PATH + "/").length(), finalNodePath.length()));
		String previousNodePath = BASE_NODE_PATH + "/" + paths.get(currentNodeIndex - 1);
		// 再次注册watcher
		try {
			zooKeeper.getData(previousNodePath, previousNodeWatcher, new Stat());
		} catch (Exception e) {
			logger.warn(String.format("Previous node watcher register failed! message: [%s]", e.getMessage()), e);
		}
	}

	private class PreviousNodeWatcher implements Watcher {

		private Leader context;

		PreviousNodeWatcher(Leader context) {
			this.context = context;
		}

		@Override
		public void process(WatchedEvent event) {
			// 节点被删除了，说明这个节点放弃了leader或者断连
			if (event.getType() == Event.EventType.NodeDeleted) {
				context.checkLeader();
			}
		}

	}

	@Override
	public String toString() {
		return "Leader{" +
				"finalNodePath='" + finalNodePath + '\'' +
				", registered=" + registered +
				", connected=" + connected +
				", leader=" + leader +
				", host='" + host + '\'' +
				", port='" + port + '\'' +
				", zooKeeper=" + zooKeeper +
				'}';
	}
}
