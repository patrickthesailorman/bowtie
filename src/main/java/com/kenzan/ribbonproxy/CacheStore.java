package com.kenzan.ribbonproxy;

import java.util.Optional;

public interface CacheStore{

    Optional<String> get(String cacheKey);

    void put(String cacheKey, String createCacheValue);
    
}