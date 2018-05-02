package com.github.johnsonmoon.zk.client;

import com.github.johnsonmoon.zk.client.core.Leader;
import com.github.johnsonmoon.zk.client.core.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xuyh at 2017/12/14 14:51.
 */
public class ZKClient {
	private static Logger logger = LoggerFactory.getLogger(ZKClient.class);
	private String zkHost = "127.0.0.1";
	private String zkPort = "2181";

	private Leader leader;
	private boolean leaderManuallyClose = false;

	public ZKClient() {
	}

	public ZKClient(String zkHost, String zkPort) {
		this.zkHost = zkHost;
		this.zkPort = zkPort;
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

	/**
	 * 创建路径为lockPath的分布式任务锁
	 *
	 * @param lockPath 分布式任务锁路径
	 * @return instance of {@link Lock}
	 */
	public Lock createLock(String lockPath) {
		return Lock.create(zkHost, zkPort, lockPath);
	}

	/**
	 * 分布式节点选举初始化
	 */
	public void leaderInit() {
		leader = new Leader(zkHost, zkPort);
		LeaderReconnectThread leaderReconnectThread = new LeaderReconnectThread();
		leaderReconnectThread.start();
	}

	/**
	 * 本进程节点是否是主节点
	 *
	 * @return
	 */
	public boolean isLeader() {
		return leader.isLeader();
	}

	/**
	 * 退出分布式节点选举
	 *
	 * @return
	 */
	public void leaderDestroy() {
		if (leader != null)
			leader.destroy();
		leaderManuallyClose = true;
	}

	/**
	 * Leader 断连 重连线程
	 * <p>
	 * <pre>
	 *  睡20秒之后查看Leader是否断连，若断连，新开一个Leader选举
	 *  同时判断Leader对象是否注册成功，若没有注册成功，新开一个Leader选举
	 * </pre>
	 *
	 * {@link Leader#connectRegister()}
	 */
	private class LeaderReconnectThread extends Thread {
		@Override
		public void run() {
			while (!leaderManuallyClose) {
				try {
					Thread.sleep(20000);
					if (leader == null) {
						leader = new Leader(zkHost, zkPort);
					} else {
						if (!leader.isConnected() || !leader.isRegistered()) {
							leader.destroy();
							leader = new Leader(zkHost, zkPort);
							System.gc();
						}
					}
				} catch (Exception e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}
}
