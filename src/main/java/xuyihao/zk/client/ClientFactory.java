package xuyihao.zk.client;

/**
 * Created by xuyh at 2017/12/14 14:49.
 */
public class ClientFactory {
	private static ZKClient zkClient;

	/**
	 * 获取ZKClient实例
	 *
	 * @return instance of {@link xuyihao.zk.client.ZKClient}
	 */
	public static ZKClient getZkClient() {
		if (zkClient == null) {
			zkClient = new ZKClient();
		}
		return zkClient;
	}

	/**
	 * 获取ZKClient实例
	 *
	 * @param zkHost
	 *            zk主机地址
	 * @param zkPort
	 *            zk服务端口
	 * @return instance of {@link xuyihao.zk.client.ZKClient}
	 */
	public static ZKClient getZKClient(String zkHost, String zkPort) {
		if (zkClient == null) {
			zkClient = new ZKClient(zkHost, zkPort);
		} else {
			zkClient.setZkHost(zkHost);
			zkClient.setZkPort(zkPort);
		}
		return zkClient;
	}
}
