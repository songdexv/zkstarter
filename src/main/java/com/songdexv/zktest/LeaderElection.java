package com.songdexv.zktest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 * 利用zookeeper实现leader选举功能
 * 
 * @author songdexv
 * 
 */
public class LeaderElection extends AbstractZKClient {
    public LeaderElection(String connectString, String root) throws IOException {
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

    void leading() {
        System.out.println("成为领导者");
    }

    void following() {
        System.out.println("成为组成员");
    }

    void findLeader() throws InterruptedException, UnknownHostException, KeeperException {
        byte[] leader = null;
        try {
            leader = zk.getData(root + "/leader", true, null);
        } catch (KeeperException e) {
            if (e instanceof KeeperException.NoNodeException) {
                e.printStackTrace();
            } else {
                throw e;
            }
        }
        if (leader != null) {
            following();
        } else {
            String newLeader = null;
            try {
                newLeader =
                        zk.create(root + "/leader", InetAddress.getLocalHost().getAddress(),
                                ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            } catch (KeeperException e) {
                if (e instanceof KeeperException.NodeExistsException) {
                    e.printStackTrace();
                } else {
                    throw e;
                }
            }
            if (newLeader != null) {
                leading();
            } else {
                mutex.wait();
            }
        }
    }

    /*
     * 
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    public void process(WatchedEvent event) {
        if (event.getPath().equals(root + "/leader") && event.getType() == Event.EventType.NodeCreated) {
            System.out.println("得到通知");
            super.process(event);
            following();
        }
    }

    public static void main(String[] args) {
        try {
            LeaderElection election =
                    new LeaderElection("127.0.0.1:2181", "/GroupMembers");
            election.findLeader();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (KeeperException e) {
            e.printStackTrace();
        }

    }
}
