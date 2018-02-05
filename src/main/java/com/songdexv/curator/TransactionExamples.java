/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.curator;

import java.util.Collection;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;

/**
 * @author songdexv
 * 
 */
public class TransactionExamples {
    private static CuratorFramework client = ClientFactory.newClient();

    public static void main(String[] args) throws Exception {
        client.start();
        // 开启事务
        CuratorTransaction transaction = client.inTransaction();
        Collection<CuratorTransactionResult> results =
                transaction.create().forPath("/a/path", "some data".getBytes()).and().setData()
                        .forPath("/another/path", "other data".getBytes()).and().delete().forPath("/yet/another/path")
                        .and().commit();
        for (CuratorTransactionResult result : results) {
            System.out.println(result.getForPath() + " - " + result.getType());
        }
    }
}
