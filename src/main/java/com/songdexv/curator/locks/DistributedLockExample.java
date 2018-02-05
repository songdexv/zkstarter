/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.curator.locks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessReadWriteLock;
import org.apache.curator.utils.CloseableUtils;

import com.google.common.collect.Lists;
import com.songdexv.curator.ClientFactory;

/**
 * 分布式锁实例
 *
 * @author songdexv
 */
public class DistributedLockExample {
    private static final String PATH = "/curator-book/locks";

    // 进程内部（可重入）读写锁
    private static InterProcessReadWriteLock lock;
    // 读锁
    private static InterProcessLock readLock;
    // 写锁
    private static InterProcessLock writeLock;

    public static void main(String[] args) throws Exception {
        CuratorFramework client = ClientFactory.newClient();
        client.start();
        lock = new InterProcessReadWriteLock(client, PATH);
        readLock = lock.readLock();
        writeLock = lock.writeLock();

        try {
            List<Thread> jobs = Lists.newArrayList();
            for (int i = 0; i < 10; i++) {
                Thread t = new Thread(new ParallelJob("Parallel任务" + i, readLock));
                jobs.add(t);
            }

            for (int i = 0; i < 10; i++) {
                Thread t = new Thread(new MutexJob("Mutex任务" + i, writeLock));
                jobs.add(t);
            }

            for (Thread t : jobs) {
                t.start();
            }
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            CloseableUtils.closeQuietly(client);
        }
    }
}
