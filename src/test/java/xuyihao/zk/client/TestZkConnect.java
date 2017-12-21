package xuyihao.zk.client;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Created by xuyh at 2017/12/20 11:14.
 */
public class TestZkConnect {
	private static String host = "127.0.0.1";
	private static String port = "2181";
	private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

	public static void main(String... args) {
		String input = "";
		while (true) {
			try {
				input = reader.readLine();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (input.equals("exit")) {
				break;
			} else if (input.contains("get ")) {
				getChildren(input.substring("get ".length(), input.length()));
			} else if (input.equals("cls") || input.equals("clear")) {
				System.out.println(
						"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			}
		}
	}

	private static void getChildren(String path) {
		try {
			ZooKeeper zooKeeper = connect();
			if (zooKeeper == null)
				return;
			boolean exist = false;
			String childrenStr = "";
			List<String> children = zooKeeper.getChildren(path, true);
			if (children != null) {
				exist = true;
				for (String s : children) {
					childrenStr += (s + "\n");
				}
			}
			System.out.println("\n------------------------------------------------------");
			System.out.println(String.format("Path [%s] exists: [%s]", path, exist));
			System.out.println("-----------------");
			System.out.println(String.format("Path [%s] children:\n%s", path, childrenStr));
			System.out.println("------------------------------------------------------\n");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static ZooKeeper connect() {
		try {
			ZooKeeper zooKeeper = new ZooKeeper(host + ":" + port, 60000, event -> {
				if (event.getState() == Watcher.Event.KeeperState.AuthFailed) {
					System.out.println("Zookeeper AuthFailed");
				} else if (event.getState() == Watcher.Event.KeeperState.Disconnected) {
					System.out.println("Zookeeper Disconnected");
				} else if (event.getState() == Watcher.Event.KeeperState.Expired) {
					System.out.println("Zookeeper Session expired");
				} else {
					if (event.getType() == Watcher.Event.EventType.None) {// 连接成功
						System.out.println("Zookeeper Connected");
					}
				}
			});
			return zooKeeper;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
