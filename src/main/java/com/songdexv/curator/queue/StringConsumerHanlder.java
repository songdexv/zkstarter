package com.songdexv.curator.queue;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.queue.QueueConsumer;
import org.apache.curator.framework.state.ConnectionState;

/**
 * @author songdexv
 * 
 */
public class StringConsumerHanlder implements QueueConsumer<String> {

    /*
     * 
     * @see
     * org.apache.curator.framework.state.ConnectionStateListener#stateChanged(org.apache.curator.framework.CuratorFramework
     * , org.apache.curator.framework.state.ConnectionState)
     */
    public void stateChanged(CuratorFramework client, ConnectionState newState) {
        System.out.println("当前状态=" + newState.name());
        System.out.println("当前data=" + client.getData());
    }

    /*
     * 
     * @see org.apache.curator.framework.recipes.queue.QueueConsumer#consumeMessage(java.lang.Object)
     */
    public void consumeMessage(String message) throws Exception {
        System.out.println("消息被消费了，消息内容=" + message);
    }

}
