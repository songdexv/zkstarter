package com.songdexv.zktest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

/**
 * Created by songdexv on 2017/8/1.
 */
public class AuthSample {
    final static String PATH = "/zk-book-auth_test";

    public static void main(String[] args) throws Exception {
        ZooKeeper zooKeeper1 = new ZooKeeper("127.0.0.1:2181", 5000, null);
        zooKeeper1.addAuthInfo("digest", "foo:true".getBytes());
        zooKeeper1.create(PATH, "init".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);

        ZooKeeper zooKeeper2 = new ZooKeeper("127.0.0.1:2181", 5000, null);
        zooKeeper2.addAuthInfo("digest", "foo:true".getBytes());
        System.out.println(zooKeeper2.getData(PATH, false, null));

        ZooKeeper zooKeeper3 = new ZooKeeper("127.0.0.1:2181", 5000, null);
        zooKeeper3.addAuthInfo("digest", "foo:false".getBytes());
        System.out.println(zooKeeper3.getData(PATH, false, null));
    }

}
