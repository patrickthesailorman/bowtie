package com.kenzan.bowtie.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.niws.client.http.CachedResponse;

public class GuavaRestCache  implements RestCache{
    
    final static private Logger LOGGER = LoggerFactory.getLogger(GuavaRestCache.class);
    
    final Cache<String, CachedResponse> cache;

    public GuavaRestCache() {
        this.cache = CacheBuilder.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public Optional<CachedResponse> get(String key) {
        LOGGER.debug("Getting cache: " + key);
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }

    @Override
    public void set(String key, CachedResponse value) {
        LOGGER.debug("Setting cache: " + key);
        cache.put(key, value);
    }
}
