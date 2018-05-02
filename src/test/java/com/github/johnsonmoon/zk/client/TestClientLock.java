package com.github.johnsonmoon.zk.client;

import com.github.johnsonmoon.zk.client.core.Lock;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuyh at 2017/12/14 15:56.
 */
public class TestClientLock {
	@Test
	public void test() {
		List<LockThread> lockThreads = new ArrayList<>();
		for (int i = 1; i <= 20; i++) {
			lockThreads.add(new LockThread(i));
		}
		lockThreads.forEach(LockThread::start);
		try {
			Thread.sleep(60000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class LockThread extends Thread {
		private int threadNumber;

		public LockThread(int threadNumber) {
			this.threadNumber = threadNumber;
		}

		@Override
		public void run() {
			Lock lock = ClientFactory.getZKClient("127.0.0.1", "2181").createLock("testTestTestLockPath_test001");
			if (lock.getLock()) {
				System.out.println(String.format("Thread [%2d] get the lock!", threadNumber));
			}
			try {
				Thread.sleep(2000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			lock.releaseLock();
		}
	}
}
