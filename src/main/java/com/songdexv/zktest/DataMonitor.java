/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.zktest;

import java.util.Arrays;

import org.apache.zookeeper.AsyncCallback.StatCallback;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * @author songdexv
 * 
 */
public class DataMonitor implements Watcher, StatCallback {
    ZooKeeper zk;

    String znode;

    Watcher chainedWatcher;

    boolean dead;

    DataMonitorListener listener;

    byte prevData[];

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher, DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;
        // Get things started by checking if the node exists. We are going to be completely event driven
        zk.exists(znode, true, this, null);
    }

    /*
     * 
     * @see org.apache.zookeeper.AsyncCallback.StatCallback#processResult(int, java.lang.String, java.lang.Object,
     * org.apache.zookeeper.data.Stat)
     */
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        boolean exists;
        if (rc == Code.OK.intValue()) {
            exists = true;
        } else if (rc == Code.NONODE.intValue()) {
            exists = false;
        } else if (rc == Code.NOAUTH.intValue() || rc == Code.SESSIONEXPIRED.intValue()) {
            dead = true;
            listener.closing(rc);
            return;
        } else {
            // Retry errors
            zk.exists(znode, true, this, null);
            return;
        }
        byte b[] = null;
        if (exists) {
            try {
                b = zk.getData(path, false, null);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        }
        if ((b == null && b != prevData) || (b != null && !Arrays.equals(b, prevData))) {
            listener.exists(b);
            prevData = b;
        }
    }

    /*
     * 
     * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
     */
    public void process(WatchedEvent event) {
        String path = event.getPath();
        if (event.getType() == EventType.None) {
            // We are being told that the state of the connection has changed
            switch (event.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(Code.SESSIONEXPIRED.intValue());
                    break;
                default:
                    break;
            }
        } else {
            if (path != null && path.equals(znode)) {
                // Something has changed on the node, let's find out
                zk.exists(path, true, this, null);
            }
            if (chainedWatcher != null) {
                chainedWatcher.process(event);
            }
        }
    }

    /**
     * Other classes use the DataMonitor by implementing this method
     */
    public interface DataMonitorListener {
        /**
         * The existence status of the node has changed.
         */
        void exists(byte data[]);

        /**
         * The ZooKeeper session is no longer valid.
         * 
         * @param sessionexpired the ZooKeeper reason code
         */
        void closing(int sessionexpired);
    }
}
