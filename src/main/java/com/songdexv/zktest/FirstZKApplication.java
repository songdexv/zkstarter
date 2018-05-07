package com.songdexv.zktest;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

/**
 * @author songdexv
 * 
 */
public class FirstZKApplication {
    public static void main(String[] args) throws Exception {
        String zkUrl = "127.0.0.1:2181";
        // 创建一个与服务器的连接
        ZooKeeper zk = new ZooKeeper(zkUrl, 10000, new Watcher() {
            public void process(WatchedEvent event) {
                System.out.println("已经触发了" + event.getType() + "事件！");
            }
        });
        // 创建一个目录节点
        zk.create("/testRootPath", "test root data".getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        // 创建一个子目录节点
        zk.create("/testRootPath/testChildPathOne", "test child data one".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(new String(zk.getData("/testRootPath", false, null)));
        // 取出子目录节点列表
        System.out.println(zk.getChildren("/testRootPath", true));
        // 修改子目录节点数据，匹配任意版本
        zk.setData("/testRootPath/testChildPathOne", "modifyChildDataOne".getBytes(), -1);
        System.out.println("目录节点状态：[" + zk.exists("/testRootPath", true) + "]");
        // 创建另外一个子目录节点
        zk.create("/testRootPath/testChildPathTwo", "testChildDataTwo".getBytes(),
                Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        System.out.println(new String(zk.getData("/testRootPath/testChildPathTwo", true, null)));
        // 删除子目录节点
        zk.delete("/testRootPath/testChildPathTwo", -1);
        zk.delete("/testRootPath/testChildPathOne", -1);
        // 删除父目录节点
        zk.delete("/testRootPath", -1);
        // 关闭连接
        zk.close();
    }
}
