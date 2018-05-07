package com.songdexv.curator.queue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author songdexv
 * 
 */
public class QueueExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            executor.execute(new Runnable() {

                public void run() {
                    try {
                        produce();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            executor.execute(new Runnable() {

                public void run() {
                    try {
                        consume();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void produce() throws Exception {
        StringQueueClient producer = null;
        try {
            // 开启生产者
            producer = new StringQueueClient(100);
            producer.start();
            for (int i = 0; i < 200; i++) {
                producer.put("I love codeing" + i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            producer.close();
        }
    }

    private static void consume() throws Exception {
        StringQueueClient consumer = null;
        try {
            // 开启消费者
            consumer = new StringQueueClient(new StringConsumerHanlder());
            consumer.start();
            // 让main程序一直监听控制台输入，不退出
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            consumer.close();
        }
    }
}
