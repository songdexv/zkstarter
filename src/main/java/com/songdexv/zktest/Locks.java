/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.zktest;

import java.util.Arrays;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 * 分布式锁实现
 *
 * @author songdexv
 */
public class Locks extends AbstractZKClient {
    String myZnode;

    /**
     * @param connectString
     */
    public Locks(String connectString, String root) {
        super(connectString);
        this.root = root;
        if (zk != null) {
            try {
                Stat stat = zk.exists(root, false);
                if (stat == null) {
                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void getLock() throws KeeperException, InterruptedException {
        System.out.println(Thread.currentThread().getName() + "尝试获取锁");
        List<String> list = zk.getChildren(root, false);
        String[] nodes = list.toArray(new String[list.size()]);
        Arrays.sort(nodes);
        if (myZnode.equals(root + "/" + nodes[0])) {
            doAction(root + "/" + nodes[0]);
        } else {
            waitForLock(nodes[0]);
        }
    }

    void doAction(String path) {
        System.out.println(Thread.currentThread().getName() + "已获得分布式锁，可以开始执行后面的任务了");
        try {
            System.out.println(Thread.currentThread().getName() + "模拟执行任务，耗时10s");
            Thread.sleep(10000);
            zk.delete(path, -1);
            System.out.println(Thread.currentThread().getName() + "任务执行结束，删除锁节点" + path);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void waitForLock(String lower) throws KeeperException, InterruptedException {
        synchronized(mutex) {
            Stat stat = zk.exists(root + "/" + lower, true);
            if (stat != null) {
                System.out.println(Thread.currentThread().getName() + " wait for lock " + lower);
                mutex.wait();
            } else {
                getLock();
            }
        }
    }

    void check() throws KeeperException, InterruptedException {
        myZnode = zk.create(root + "/lock_", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println(Thread.currentThread().getName() + " create path " + myZnode);
        getLock();
    }

    /*
     * 
     * @see com.songdexv.zktest.AbstractZKClient#process(org.apache.zookeeper.WatchedEvent)
     */
    @Override
    public void process(WatchedEvent event) {
        if (event.getType() == EventType.NodeDeleted) {
            System.out.println(Thread.currentThread().getName() + "得到通知");
            super.process(event);
            doAction(event.getPath());
        }
    }

    public static void main(String[] args) {
        final Locks lock1 =
                new Locks("127.0.0.1:2181", "/locks");
        final Locks lock2 =
                new Locks("127.0.0.1:2181", "/locks");
        Thread thread1 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    lock1.check();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        Thread thread2 = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    lock2.check();
                } catch (KeeperException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread1.start();
        thread2.start();
    }
}
