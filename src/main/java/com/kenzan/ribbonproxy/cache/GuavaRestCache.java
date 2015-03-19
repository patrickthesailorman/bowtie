/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GuavaRestCache  implements RestCache{

    final Cache<String, Object> cache;

    public GuavaRestCache() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Optional<Object> get(String key) {
        return Optional.ofNullable(cache.getIfPresent(key));
    }

    @Override
    public void set(String key, Object value) {
        cache.put(key, value);
    }
}
