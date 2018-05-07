package com.songdexv.zktest;

import java.io.IOException;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author songdexv
 * 
 */
public class AbstractZKClient implements Watcher {
    protected static ZooKeeper zk = null;
    protected static Integer mutex;
    int sessionTimeout = 10000;
    protected String root;

    public AbstractZKClient(String connectString) {
        if (zk == null) {
            try {

                System.out.println("创建一个新的连接:");
                zk = new ZooKeeper(connectString, sessionTimeout, this);
                mutex = new Integer(-1);
            } catch (IOException e) {
                zk = null;
            }
        }
    }

    /*
     * 
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    synchronized public void process(WatchedEvent event) {
        synchronized (mutex) {
            mutex.notifyAll();
        }
    }
}
