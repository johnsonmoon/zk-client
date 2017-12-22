package xuyihao.zk.client.core;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xuyihao.zk.client.core.common.Operations;

import java.util.List;

/**
 * Created by xuyh at 2017/11/29 19:00.
 * 
 * <pre>
 * 子节点监听方式
 *
 * 实现思路：监听子节点状态
 * 1.在父节点(持久化)下创建临时节点，实际创建的节点路径会根据数量进行自增(ZK自编号方式创建节点)。
 * 2.创建节点成功后，首先获取父节点下的子节点列表，判断本线程的路径后缀编号是否是所有子节点中最小的，若是则获取锁，反之监听本节点前一个节点(路径排序为本节点路径数字减一的节点)变动状态(通过getData()方法注册watcher)
 * 3.当监听对象状态变动(节点删除状态)后watcher会接收到通知，这时再次判断父节点下的子节点的排序状态，若满足本线程的路径后缀编号最小则获取锁，反之继续注册watcher监听前一个节点状态
 * </pre>
 */
public class Lock {
	private static Logger logger = LoggerFactory.getLogger(Lock.class);
	private static final String CHILD_NODE_PATH = "temp";
	private String baseLockPath;
	private String finalLockId;
	private String host = "127.0.0.1";
	private String port = "2181";
	private ZooKeeper zooKeeper;
	private PreviousNodeWatcher previousNodeWatcher;

	private boolean needInterrupt = false;// 是否需要中断阻塞标志位
	private boolean connected = false;// ZK是否连接成功标志位
	private boolean acquireLock = false;// 是否获取到锁标志位

	private Lock(String host, String port, String lock) {
		this.host = host;
		this.port = port;
		this.baseLockPath = "/" + lock;
		this.previousNodeWatcher = new PreviousNodeWatcher(this);
	}

	/**
	 * 新建锁(连接ZK阻塞)
	 *
	 * @param host
	 *            zk 服务ip
	 * @param port
	 *            zk 服务端口
	 * @param lock
	 *            锁名称
	 * @return
	 */
	public static Lock create(String host, String port, String lock) {
		Lock zkLock = new Lock(host, port, lock);
		zkLock.connectRegister();
		return zkLock;
	}

	/**
	 * 获取锁(阻塞)
	 *
	 * @return true代表获取到分布式任务锁
	 */
	public boolean getLock() {
		if (!connected)
			return false;
		while (!needInterrupt) {
			if (acquireLock) {
				return true;
			}

			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}
		return false;
	}

	/**
	 * 释放锁
	 *
	 * @return true代表释放锁成功, 并切断ZK连接
	 */
	public boolean releaseLock() {
		try {
			if (zooKeeper != null && connected) {
				zooKeeper.delete(finalLockId, -1);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
		return disconnectZooKeeper();
	}

	private boolean disconnectZooKeeper() {
		if (zooKeeper == null && !connected)
			return false;
		try {
			connected = false;
			acquireLock = false;
			zooKeeper.close();
		} catch (Exception e) {
			logger.warn(String.format("ZK disconnect failed. [%s]", e.getMessage()), e);
		}
		return true;
	}

	/**
	 * 连接并注册锁竞争
	 * 
	 * @return
	 */
	private void connectRegister() {
		try {
			// 连接ZK
			zooKeeper = new ZooKeeper(host + ":" + port, 60000, event -> {
				if (event.getState() == Watcher.Event.KeeperState.AuthFailed) {
					connected = false;
					needInterrupt = true;
				} else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
					connected = false;
					needInterrupt = true;
				} else if (event.getState() == Watcher.Event.KeeperState.Expired) {
					connected = false;
					needInterrupt = true;
				} else {
					if (event.getType() == Watcher.Event.EventType.None) {// 连接成功
						connected = true;
					}
				}
			});

			// 等待异步连接成功,超过时间30s则退出等待，防止线程锁死
			int i = 1;
			while (!connected) {
				if (i == 100)
					break;
				Thread.sleep(300);
				i++;
			}

			if (connected) {
				// 创建父节点
				if (zooKeeper.exists(baseLockPath, false) == null) {
					zooKeeper.create(baseLockPath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				}

				// 创建子节点
				finalLockId = zooKeeper.create(baseLockPath + "/" + CHILD_NODE_PATH, "".getBytes(),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

				// 检查一次是否获取到锁
				checkAcquire();
			} else {
				needInterrupt = true;
				logger.warn("Connect zookeeper failed. Time consumes 30 s");
			}
		} catch (Exception e) {
			needInterrupt = true;
			logger.warn(e.getMessage(), e);
		}
	}

	private void checkAcquire() {
		if (!connected)
			return;
		try {
			// 获取子节点列表，若没有获取到锁，注册监听，监听对象应当是比本节点路径编号小一(或者排在前面一位)的节点
			List<String> childrenList = zooKeeper.getChildren(baseLockPath, false);

			if (Operations.judgePathNumMin(baseLockPath, CHILD_NODE_PATH, finalLockId, childrenList)) {
				acquireLock = true;// 获取到锁
			} else {
				watchPreviousNode(childrenList);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
			disconnectZooKeeper();
		}
	}

	private void watchPreviousNode(List<String> paths) {
		if (paths.isEmpty() || paths.size() == 1) {
			needInterrupt = true;
			return;
		}
		int currentNodeIndex = paths
				.indexOf(finalLockId.substring((baseLockPath + "/").length(), finalLockId.length()));
		String previousNodePath = baseLockPath + "/" + paths.get(currentNodeIndex - 1);
		// 通过getData方法注册watcher
		try {
			zooKeeper.getData(previousNodePath, previousNodeWatcher, new Stat());
		} catch (Exception e) {
			// watcher注册失败,退出锁竞争
			logger.warn(String.format("Previous node watcher register failed! message: [%s]", e.getMessage()), e);
			needInterrupt = true;
		}
	}

	private class PreviousNodeWatcher implements Watcher {
		private Lock context;

		PreviousNodeWatcher(Lock context) {
			this.context = context;
		}

		@Override
		public void process(WatchedEvent event) {
			// 节点被删除了，说明这个节点释放了锁
			if (event.getType() == Event.EventType.NodeDeleted) {
				context.checkAcquire();
			}
		}
	}
}
