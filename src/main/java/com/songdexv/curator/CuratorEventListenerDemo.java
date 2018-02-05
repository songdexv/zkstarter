/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.curator;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;

/**
 * @author songdexv
 */
public class CuratorEventListenerDemo {
    public static void main(String[] args) {
        CuratorFramework client = ClientFactory.newClient();
        client.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            public void stateChanged(CuratorFramework client, ConnectionState newState) {
                if (newState == ConnectionState.LOST) {
                    while (true) {
                        try {
                            System.out.println(Thread.currentThread().getName() + " 连接端口，重新恢复连接");
                            if (client.getZookeeperClient().blockUntilConnectedOrTimedOut()) {
                                System.out.println(Thread.currentThread().getName() + " 连接恢复");
                                break;
                            }
                        } catch (InterruptedException e) {
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        client.start();
        try {
            client.delete().forPath("/zk-event/cnode");
            client.create().creatingParentsIfNeeded().forPath("/zk-event/cnode", "hello".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExecutorService pool = Executors.newFixedThreadPool(2);
        final NodeCache nodeCache = new NodeCache(client, "/zk-event/cnode", false);
        nodeCache.getListenable().addListener(new NodeCacheListener() {
            public void nodeChanged() throws Exception {
                System.out.println(Thread.currentThread().getName() + " Node data is changed, new data: "
                        + new String(nodeCache.getCurrentData().getData()));
            }
        }, pool);
        try {
            nodeCache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        final PathChildrenCache childrenCache = new PathChildrenCache(client, "/zk-event", true);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        System.out.println(Thread.currentThread().getName() + " CHILD_ADDED: " + event.getData()
                                .getPath());
                        break;
                    case CHILD_REMOVED:
                        System.out.println(Thread.currentThread().getName() + " CHILD_REMOVED: " + event.getData()
                                .getPath());
                        break;
                    case CHILD_UPDATED:
                        System.out.println(Thread.currentThread().getName() + " CHILD_UPDATED: " + event.getData()
                                .getPath());
                        break;
                    default:
                        break;
                }
            }
        }, pool);
        try {
            childrenCache.start();

            client.setData().forPath("/zk-event/cnode", "world".getBytes());
            Thread.sleep(1000);

            client.create().forPath("/zk-event/anode");
            Thread.sleep(1000);

            client.setData().forPath("/zk-event/anode", "anode".getBytes());
            Thread.sleep(5 * 1000);

            client.delete().forPath("/zk-event/anode");
            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
            try {
                childrenCache.close();
                nodeCache.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.close();
        }
    }
}
