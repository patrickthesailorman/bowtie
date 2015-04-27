package com.kenzan.bowtie.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.netflix.niws.client.http.CachedResponse;

/***
 * <p>
 * Guava cache based {@link RestCache}.  Caches responses in a Guava {@link Cache}.    Use GuavaRestCache.newDefaultCache()
 * to create a default cache.  The default cache ignores the HTTP cache headers and caches responses for ten minutes.
 * </p>
 *
 */
public class GuavaRestCache  implements RestCache{
    
    final static private Logger LOGGER = LoggerFactory.getLogger(GuavaRestCache.class);
    
    final Cache<String, CachedResponse> cache;

    
    public static GuavaRestCache newDefaultCache() {
        return new GuavaRestCache(CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build());
    }
    
    public GuavaRestCache(Cache<String, CachedResponse> cache) {
        this.cache = cache;
    }

    @Override
    public Optional<CachedResponse> get(String key) {
        LOGGER.debug("Getting cache: {}", key);
        return Optional.ofNullable(this.cache.getIfPresent(key));
    }

    @Override
    public void set(String key, CachedResponse value) {
        LOGGER.debug("Setting cache: {}", key);
        cache.put(key, value);
    }
}
