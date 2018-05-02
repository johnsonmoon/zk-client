package com.github.johnsonmoon.zk.client;

/**
 * 启动多个测试进程，并观察控制台消息，适时结束其中单个进程，观察控制台消息
 * <p>
 * Created by xuyh at 2017/12/14 15:57.
 */
public class TestClientLeader {
	public static void main(String... args) {
		ClientFactory.getZKClient("127.0.0.1", "2181").leaderInit();
		boolean interrupt = false;
		int count = 0;
		while (!interrupt) {
			count++;
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println(
					"\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n");
			System.out.println(String.format("[%3d] Is leader? : [%s]", count, ClientFactory.getZkClient().isLeader()));
			if (count == 600) {
				interrupt = true;
			}
		}
	}
}
