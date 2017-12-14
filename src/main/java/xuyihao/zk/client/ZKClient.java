package xuyihao.zk.client;

import xuyihao.zk.client.core.Leader;
import xuyihao.zk.client.core.Lock;

/**
 * Created by xuyh at 2017/12/14 14:51.
 */
public class ZKClient {
	private String zkHost = "127.0.0.1";
	private String zkPort = "2181";

	public ZKClient() {
	}

	public ZKClient(String zkHost, String zkPort) {
		this.zkHost = zkHost;
		this.zkPort = zkPort;
	}

	/**
	 * 创建路径为lockPath的分布式任务锁
	 *
	 * @param lockPath
	 *            分布式任务锁路径
	 * @return instance of {@link xuyihao.zk.client.core.Lock}
	 */
	public Lock createLock(String lockPath) {
		return Lock.create(zkHost, zkPort, lockPath);
	}

	/**
	 * 分布式节点选举初始化
	 * <p>
	 * 
	 * <pre>
	 *  调用 {@link Leader#isLeader()} 方法获取节点主从状态
	 * </pre>
	 *
	 * @return true - 成功 false - 失败
	 */
	public boolean leaderInit() {
		return Leader.init(zkHost, zkPort);
	}

	public String getZkHost() {
		return zkHost;
	}

	public void setZkHost(String zkHost) {
		this.zkHost = zkHost;
	}

	public String getZkPort() {
		return zkPort;
	}

	public void setZkPort(String zkPort) {
		this.zkPort = zkPort;
	}
}
