package com.kenzan.bowtie.cache;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.collect.Lists;
import com.netflix.evcache.EVCache;
import com.netflix.evcache.EVCacheException;
import com.netflix.evcache.pool.EVCacheClientPoolManager;
import com.netflix.niws.client.http.CachedResponse;
import com.thimbleware.jmemcached.CacheImpl;
import com.thimbleware.jmemcached.Key;
import com.thimbleware.jmemcached.LocalCacheElement;
import com.thimbleware.jmemcached.MemCacheDaemon;
import com.thimbleware.jmemcached.storage.CacheStorage;
import com.thimbleware.jmemcached.storage.hash.ConcurrentLinkedHashMap;

public class MemcacheRestCacheTest {

    final static private String FOO = "foo";
    
    private static HashMap<String, Collection<String>> newHeaders() {
        HashMap<String, Collection<String>> map = new HashMap<>();
        map.put(FOO, Lists.newArrayList("bar"));
        
        return map;
    }
    
    final private MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();
    
    @Before
    public void setup() throws InterruptedException{
        
        // create daemon and start it
        final CacheStorage<Key, LocalCacheElement> storage = ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO,
            10000, 10000);
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(true);
        daemon.setAddr(new InetSocketAddress(11242));
        daemon.setIdleTime(1000);
        daemon.setVerbose(true);
        daemon.start();
    }

    @Test
    public void testSetAndGet() {
        
        EVCacheClientPoolManager.getInstance().initEVCache("SAMPLECACHE");
        
        EVCache evCache = (new EVCache.Builder())
                        .setAppName("SAMPLECACHE")
                        .setCacheName("cid")
                        .enableZoneFallback()
                        .build();
        
        final MemcacheRestCache memcacheRestCache = new MemcacheRestCache(evCache);
        final Map<String, Collection<String>> headers = newHeaders();
        memcacheRestCache.set(FOO, new CachedResponse(HttpStatus.SC_OK, headers, FOO.getBytes()));
        
        final CachedResponse cachedResponse = memcacheRestCache.get(FOO).get();
        
        Assert.assertThat(cachedResponse.getStatus(), IsEqual.equalTo(HttpStatus.SC_OK));
        Assert.assertThat(cachedResponse.getCachedBytes(), IsEqual.equalTo(FOO.getBytes()));
        Assert.assertThat(cachedResponse.getHeaders(), IsEqual.equalTo(headers));
    }
    
    @Test
    public void testSetException() throws EVCacheException {
        
        CachedResponse cachedResponse = new CachedResponse(HttpStatus.SC_OK, null, null);
        
        final EVCache evcache = Mockito.mock(EVCache.class);
        
        Mockito.doThrow(new EVCacheException("Set me an exception!"))
            .when(evcache) 
            .set(FOO, cachedResponse, MemcacheRestCache.TRANSCODER);

        final MemcacheRestCache memcacheRestCache = new MemcacheRestCache(evcache);
        
        try {
            memcacheRestCache.set(FOO, cachedResponse);
        } catch (Exception e) {
            Assert.assertThat(e.getClass(), IsEqual.equalTo(MemcacheRestCache.MemcacheRestCacheException.class));
            Assert.assertThat(e.getMessage(), IsEqual.equalTo("Could set key foo"));
        }
    }
    
     
    @Test
    public void testGetException() throws EVCacheException {
        
        final EVCache evcache = Mockito.mock(EVCache.class);
        
        Mockito.doThrow(new EVCacheException("Get me an exception!"))
            .when(evcache)
            .get(FOO, MemcacheRestCache.TRANSCODER);

        final MemcacheRestCache memcacheRestCache = new MemcacheRestCache(evcache);
        
        try {
            memcacheRestCache.get(FOO);
        } catch (Exception e) {
            Assert.assertThat(e.getClass(), IsEqual.equalTo(MemcacheRestCache.MemcacheRestCacheException.class));
            Assert.assertThat(e.getMessage(), IsEqual.equalTo("Could get key foo"));
        }
    }
    
    
    @After
    public void teardown(){
        daemon.stop();
    }
}
