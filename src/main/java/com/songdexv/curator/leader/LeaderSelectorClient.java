/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.curator.leader;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListenerAdapter;

/**
 * 本类基于leaderSelector实现,所有存活的client会公平的轮流做leader 如果不想频繁的变化Leader，需要在takeLeadership方法里阻塞leader的变更！ 或者使用 {@link}
 * LeaderLatchClient
 * 
 * @author songdexv
 * 
 */
public class LeaderSelectorClient extends LeaderSelectorListenerAdapter implements Closeable {
    private final String name;
    private final LeaderSelector leaderSelector;
    private final String PATH = "/leaderselector";

    public LeaderSelectorClient(CuratorFramework client, String name) {
        this.name = name;
        leaderSelector = new LeaderSelector(client, PATH, this);
        leaderSelector.autoRequeue();
    }

    /*
     * 
     * @see
     * org.apache.curator.framework.recipes.leader.LeaderSelectorListener#takeLeadership(org.apache.curator.framework
     * .CuratorFramework)
     */
    public void takeLeadership(CuratorFramework client) throws Exception {
        int waitSeconds = (int) (5 * Math.random()) + 1;
        System.out.println(name + "是当前的leader");
        try {
            Thread.sleep(TimeUnit.SECONDS.toMillis(waitSeconds));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            System.out.println(name + " 让出领导权\n");
        }
    }

    /*
     * 
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        leaderSelector.close();
    }

    public void start() throws IOException {
        leaderSelector.start();
    }

    /**
     * 是否为leader
     * 
     * @return
     */
    public boolean isLeader() {
        return leaderSelector.hasLeadership();
    }

    /**
     * 让出领导权
     */
    public void release() {
        leaderSelector.interruptLeadership();
    }

    /***
     * 试图重新获取领导权
     */
    public void take() {
        leaderSelector.requeue();
    }
}
