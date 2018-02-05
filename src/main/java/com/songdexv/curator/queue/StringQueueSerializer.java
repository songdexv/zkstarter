/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.songdexv.curator.queue;

import java.nio.charset.Charset;

import org.apache.curator.framework.recipes.queue.QueueSerializer;

/**
 * @author songdexv
 * 
 */
public class StringQueueSerializer implements QueueSerializer<String> {
    private static final Charset charset = Charset.forName("utf-8");

    /*
     * 
     * @see org.apache.curator.framework.recipes.queue.QueueSerializer#serialize(java.lang.Object)
     */
    public byte[] serialize(String item) {
        return item.getBytes(charset);
    }

    /*
     * 
     * @see org.apache.curator.framework.recipes.queue.QueueSerializer#deserialize(byte[])
     */
    public String deserialize(byte[] bytes) {
        return new String(bytes, charset);
    }

}
