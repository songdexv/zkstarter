package com.songdexv.curator.locks;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import com.songdexv.curator.ClientFactory;

/**
 * @author songdexv
 * 
 */
public class SharedLockExample {
    private static final String PATH = "/sharedlocks";
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        new Job("Client" + Thread.currentThread().getName(), PATH, ClientFactory.newClient()).doWork();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        executor.shutdown();
    }
}

class Job {
    private String name;
    private CuratorFramework client;
    private String path;

    public Job(String name, String path, CuratorFramework client) {
        this.name = name;
        this.path = path;
        this.client = client;
    }

    public void doWork() throws Exception {
        client.start();
        InterProcessSemaphoreMutex mutex = new InterProcessSemaphoreMutex(client, path);
        try {
            if (!mutex.acquire(1, TimeUnit.SECONDS)) {
                System.out.println(name + " 尝试获取锁失败");
                return;
            }
            TimeUnit.MILLISECONDS.sleep(4000);
            System.out.println(name + " do some work");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                mutex.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
