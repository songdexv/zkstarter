package com.songdexv.zktest;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by songdexv on 2017/7/31.
 */
public class ZKConstructorUsage implements Watcher {
    private static CountDownLatch latch = new CountDownLatch(1);

    public static void main(String[] args) throws Exception {
        ZooKeeper zk = new ZooKeeper("127.0.0.1:2181", 5000,
                new ZKConstructorUsage());
        System.out.println(zk.getState());
        try {
            latch.await();
        } catch (InterruptedException e) {

        }
        System.out.println("Zookeeper session established");
    }

    @Override
    public void process(WatchedEvent event) {
        System.out.println("receive watched event: " + event);
        if (Event.KeeperState.SyncConnected == event.getState()) {
            latch.countDown();
        }
    }
}
