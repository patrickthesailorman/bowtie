/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GuavaRestCache  implements RestCache{

    final static private Logger LOGGER = LoggerFactory.getLogger(GuavaRestCache.class);
    
    final Cache<String, byte[]> cache;

    public GuavaRestCache() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Optional<byte[]> get(String key) {
        
        Optional<byte[]> bytes = Optional.ofNullable(cache.getIfPresent(key));
        LOGGER.info("Found in cache: " + key + " " + bytes.isPresent());
        return bytes;
    }

    @Override
    public void set(String key, byte[] value) {
        
        LOGGER.info("Setting cache: " + key);
        cache.put(key, value);
    }
}
