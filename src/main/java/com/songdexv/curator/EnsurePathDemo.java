package com.songdexv.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.EnsurePath;

/**
 * Created by songdexv on 2017/8/3.
 */
public class EnsurePathDemo {
    static CuratorFramework client = ClientFactory.newClient();
    static String path = "/curator-book/c1";

    public static void main(String[] args) throws Exception {
        client.start();
        client.usingNamespace("curator-book");
        EnsurePath ensurePath = new EnsurePath(path);
        ensurePath.ensure(client.getZookeeperClient());
        ensurePath.ensure(client.getZookeeperClient());

        EnsurePath ensurePath1 = client.newNamespaceAwareEnsurePath("/c1");
        ensurePath1.ensure(client.getZookeeperClient());
    }
}
