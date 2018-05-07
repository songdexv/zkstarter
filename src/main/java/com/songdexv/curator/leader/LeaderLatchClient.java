package com.songdexv.curator.leader;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;

/**
 * leader选举
 * 
 * @author songdexv
 * 
 */
public class LeaderLatchClient implements Closeable {
    private final LeaderLatch leaderLatch;
    private final String PATH = "/leaderlatch";
    private static Executor executor = Executors.newCachedThreadPool();

    public LeaderLatchClient(CuratorFramework client, final String name) {
        this.leaderLatch = new LeaderLatch(client, PATH);
        LeaderLatchListener latchListener = new LeaderLatchListener() {

            public void notLeader() {
                System.out.println("I release my leader ship, my name is " + name);
            }

            public void isLeader() {
                System.out.println("I am leader, my name is " + name);
            }
        };
        leaderLatch.addListener(latchListener, executor);
    }

    /*
     * 
     * @see java.io.Closeable#close()
     */
    public void close() throws IOException {
        leaderLatch.close();
    }

    public void start() throws Exception {
        leaderLatch.start();
    }

    public boolean isLeader() {
        return leaderLatch.hasLeadership();
    }
}
