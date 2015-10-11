package com.kenzan.bowtie.cache;

import java.util.Optional;

import com.netflix.niws.client.http.CachedResponse;

/***
 * <p>
 * Key/Value Interface for caching {@link CachedResponse} objects
 * </p> 
 */
public interface RestCache {

    /***
     * Gets the {@link CachedResponse}
     * 
     * @param key the key for CachedResponse
     * @return an {@link Optional} of {@link CachedResponse}
     */
    public Optional<CachedResponse> get(String key);

    
    /***
     * Sets the {@link CachedResponse}
     * 
     * @param key the key
     * @param httpResponse the value
     */
    public void set(String key, CachedResponse httpResponse);
}
