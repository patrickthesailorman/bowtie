package com.kenzan.bowtie.cache;

import java.util.Optional;

import com.netflix.niws.client.http.CachedResponse;

public interface RestCache {

    public Optional<CachedResponse> get(String key);

    public void set(String key, CachedResponse httpResponse);
}
