package com.songdexv.zkclient;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * Created by songdexv on 2017/8/1.
 */
public class GetDataSample {
    public static void main(String[] args) throws Exception{
        String path = "/zkclient-test";
        ZkClient zkClient = new ZkClient("127.0.0.1:2181",5000);
        zkClient.createEphemeral(path,"123");
        zkClient.subscribeDataChanges(path, new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object o) throws Exception {
                System.out.println("Node " + dataPath + " changed, new data: " + o);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                System.out.println("Node " + s + " deleted");
            }
        });

        System.out.println(zkClient.readData(path));
        zkClient.writeData(path,"456");
        Thread.sleep(1000);
        zkClient.delete(path);
        Thread.sleep(10000);
    }
}
