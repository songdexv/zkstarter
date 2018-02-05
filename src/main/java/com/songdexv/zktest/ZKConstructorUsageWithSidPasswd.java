package com.songdexv.zktest;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by songdexv on 2017/7/31.
 */
public class ZKConstructorUsageWithSidPasswd implements Watcher {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 5000,
                new ZKConstructorUsage());
        latch.await();
        long sessionId = zk.getSessionId();
        byte[] passwd = zk.getSessionPasswd();

        //use illegal sessionid and sessionPasswd
        zk = new ZooKeeper("127.0.0.1:2181", 5000, new
                ZKConstructorUsageWithSidPasswd(), 1l, "test".getBytes());

        //use correct sessionId and sessionPwd
        zk = new ZooKeeper("127.0.0.1:2181", 5000, new
                ZKConstructorUsageWithSidPasswd(), sessionId, passwd);
        Thread.sleep(10000);
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("receive watched event: " + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            latch.countDown();
        }
    }
}
