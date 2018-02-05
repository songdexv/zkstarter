package com.songdexv.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.zookeeper.CreateMode;

/**
 * Created by songdexv on 2017/8/2.
 */
public class NodeCacheSample {
    static String path = "/curator-book/nodecache";
    static CuratorFramework client = ClientFactory.newClient();

    public static void main(String[] args) throws Exception {
        client.start();
//        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(path, "init".getBytes());
//
//        final NodeCache nodeCache = new NodeCache(client,path,false);
//        nodeCache.start(true);
//        nodeCache.getListenable().addListener(new NodeCacheListener() {
//            @Override
//            public void nodeChanged() throws Exception {
//                System.out.println("Node data update, new data: " + new String(nodeCache.getCurrentData().getData()));
//            }
//        });
//        client.setData().forPath(path,"update".getBytes());
//        Thread.sleep(1000);
//        client.delete().guaranteed().deletingChildrenIfNeeded().forPath(path);
//        Thread.sleep(5000);
        client.setData().forPath("/servers/supergw","10.46.139.37:8092,10.100.19.174:8092".getBytes());
    }
}
