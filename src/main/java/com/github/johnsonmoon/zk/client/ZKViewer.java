package com.github.johnsonmoon.zk.client;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.List;
import java.util.Locale;

/**
 * zk server 内部数据结构查看程序
 * <p>
 * Created by xuyh at 2017/12/20 11:14.
 */
public class ZKViewer {
    private static String host = "192.168.1.215";//10.1.60.240
    private static String port = "2181";
    private static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String... args) {
        System.out.println("Input \"help\" for more ... ...");
        String input = "";
        while (true) {
            try {
                input = reader.readLine();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (input.equals("exit")) {
                System.out.println("Bye ...");
                break;
            } else if (input.contains("host ")) {
                host = input.substring("host ".length(), input.length());
                System.out.println(String.format("Host [%s] set.", host));
            } else if (input.contains("port ")) {
                port = input.substring("port ".length(), input.length());
                System.out.println(String.format("Port [%s] set.", port));
            } else if (input.contains("child ")) {
                getChildren(input.substring("child ".length(), input.length()));
            } else if (input.contains("data ")) {
                getData(input.substring("data ".length(), input.length()));
            } else if (input.equals("cls") || input.equals("clear")) {
                System.out.println(
                        "\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
            } else if (input.equals("help")) {
                System.out.println("Commands: ");
                System.out.println("-------------------");
                System.out.println("host  -  setting zk host");
                System.out.println("port  -  setting zk port");
                System.out.println("-------------------");
                System.out.println("help  -  print   helps ");
                System.out.println("clear -  clear   screen ");
                System.out.println("cls   -  clear   screen ");
                System.out.println("exit  -  exit    the program ");
                System.out.println("-------------------");
                System.out.println("child -  print   children  of an existing zookeeper node ");
                System.out.println("data  -  print   data      of an existing zookeeper node ");
            }
        }
        System.exit(0);
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
                children.sort(Collator.getInstance(Locale.ENGLISH));
                exist = true;
                for (String s : children) {
                    childrenStr += (s + "\n");
                }
            }
            System.out.println("------------------------------------------------------");
            System.out.println(String.format("Path [%s] exists: [%s]", path, exist));
            System.out.println("-----------------");
            System.out.println(String.format("Path [%s] children:\n%s", path, childrenStr));
            System.out.println("------------------------------------------------------\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void getData(String path) {
        try {
            ZooKeeper zooKeeper = connect();
            if (zooKeeper == null)
                return;
            boolean exist = false;
            String dataStr = "";
            byte[] data = zooKeeper.getData(path, true, new Stat());
            if (data != null) {
                exist = true;
                dataStr = new String(data);
            }
            System.out.println("------------------------------------------------------");
            System.out.println(String.format("Path [%s] exists: [%s]", path, exist));
            System.out.println("-----------------");
            System.out.println(String.format("Path [%s] data:\n%s", path, dataStr));
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
