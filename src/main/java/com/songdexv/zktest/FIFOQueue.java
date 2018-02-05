/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.zktest;

import java.nio.ByteBuffer;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

/**
 * 分布式FIFO队列
 * 
 * @author songdexv
 * 
 */
public class FIFOQueue extends AbstractZKClient {

    /**
     * @param connectString
     */
    public FIFOQueue(String connectString, String root) {
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

    boolean produce(int i) throws KeeperException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        byte[] value;
        value = buffer.array();
        String node = zk.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
        System.out.println("create node: " + node);
        return true;
    }

    int consume() throws KeeperException, InterruptedException {
        int retvalue = -1;
        Stat stat = null;
        while (true) {
            synchronized (mutex) {
                List<String> list = zk.getChildren(root, true);
                if (list.size() == 0) {
                    mutex.wait();
                } else {
                    String minStr = list.get(0).substring(7);
                    Integer min = new Integer(minStr);
                    for (String s : list) {
                        String tempStr = s.substring(7);
                        Integer tempValue = new Integer(tempStr);
                        if (tempValue < min) {
                            min = tempValue;
                            minStr = tempStr;
                        }
                    }
                    byte[] b = zk.getData(root + "/element" + minStr, false, stat);
                    zk.delete(root + "/element" + minStr, 0);
                    ByteBuffer buffer = ByteBuffer.wrap(b);
                    retvalue = buffer.getInt();
                    return retvalue;
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.songdexv.zktest.AbstractZKClient#process(org.apache.zookeeper.WatchedEvent)
     */
    @Override
    public synchronized void process(WatchedEvent event) {
        super.process(event);
    }

    public static void main(String[] args) {
        FIFOQueue queue =
                new FIFOQueue("127.0.0.1:2181", "/queue");
        int i;
        Integer max = new Integer(2);

        System.out.println("Producer");
        for (i = 0; i < max; i++) {
            try {
                queue.produce(10 + i);
            } catch (KeeperException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (i = 0; i < max; i++) {
            try {
                int r = queue.consume();
                System.out.println("Item: " + r);
            } catch (KeeperException e) {
                i--;
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
