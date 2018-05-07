package com.songdexv.zktest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.List;
import java.util.Scanner;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;

/**
 * 当一个队列的成员都聚齐时，这个队列才可用，否则一直等待所有成员到达，这种是同步队列
 *
 * @author songdexv
 */
public class Synchronizing extends AbstractZKClient {
    int size;
    String name;

    /**
     * @param connectString
     */
    public Synchronizing(String connectString, String root, int size) {
        super(connectString);
        this.root = root;
        this.size = size;
        if (zk != null) {
            try {
                Stat stat = zk.exists(root, false);
                if (stat == null) {
                    zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            name = InetAddress.getLocalHost().getCanonicalHostName().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行其他任务
     */
    private void doAction() {
        System.out.println("同步队列已经得到同步，可以开始执行后面的任务了");
    }

    void addQueue() throws KeeperException, InterruptedException {
        zk.exists(root + "/start", true);
        zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        synchronized(mutex) {
            List<String> list = zk.getChildren(root, false);
            if (list.size() < size) {
                mutex.wait();
            } else {
                zk.create(root + "/start", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        }
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getPath().equals(root + "/start") && event.getType() == Event.EventType.NodeCreated) {
            System.out.println("得到通知");
            super.process(event);
            doAction();
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            Synchronizing sync =
                    new Synchronizing("127.0.0.1:2181", "/synchronizing",
                            2);
            sync.addQueue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }
    }
}
