package com.kenzan.bowtie.cache;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.netflix.evcache.EVCacheTranscoder;
import org.apache.http.HttpStatus;
import org.hamcrest.core.IsEqual;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
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

/***
 * <p>
 *  Test for {@link MemcacheRestCache}.  Uses jmemcached, which doesn't currently honor TTLs.
 * </p> 
 */
public class MemcacheRestCacheTest {

    final static private String FOO = "foo";
    
    private static HashMap<String, Collection<String>> newHeaders() {
        HashMap<String, Collection<String>> map = new HashMap<>();
        map.put(FOO, Lists.newArrayList("bar"));
        map.put("Cache-Control", Lists.newArrayList("no-transform,public,max-age=2,s-maxage=900"));
        
        return map;
    }
    
    final private MemCacheDaemon<LocalCacheElement> daemon = new MemCacheDaemon<LocalCacheElement>();
    
    @Before
    public void setup() throws InterruptedException{

        // create daemon and start it
        final CacheStorage<Key, LocalCacheElement> storage =
                        ConcurrentLinkedHashMap.create(ConcurrentLinkedHashMap.EvictionPolicy.FIFO,
            10000, 10000);
        daemon.setCache(new CacheImpl(storage));
        daemon.setBinary(true);
        daemon.setAddr(new InetSocketAddress(11242));
        daemon.setIdleTime(1000);
        daemon.setVerbose(true);
        daemon.start();
    }

    public void testSetAndGet() throws InterruptedException {
        
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

    /**
     * verify that when TTL is under 2592000 seconds, the value is NOT
     * converted to a unix timestamp.
     *
     * @throws EVCacheException
     */
    @Test
    public void testSetTTLSeconds() throws EVCacheException {
        final EVCache evCache = Mockito.mock(EVCache.class);
        final String key = "KEY";

        final CachedResponse cachedResponse = Mockito.mock(CachedResponse.class);
        final MemcacheRestCache memcacheRestCache = new MemcacheRestCache(evCache);
        final ArgumentCaptor<Integer> ttlCaptor = ArgumentCaptor.forClass(Integer.class);

        Mockito.when(cachedResponse.getTTL()).thenReturn(2591999L);
        memcacheRestCache.set(key, cachedResponse);

        Mockito.verify(evCache)
                        .set(Matchers.eq(key), Matchers.any(), Matchers.any(EVCacheTranscoder.class), ttlCaptor.capture());

        int capturedTTL = ttlCaptor.getValue();

        Assert.assertEquals(2591999L, capturedTTL);

    }

    /**
     * Verify that when TTL is over 2592000L, a unix timestamp is used as the memcache TTL.
     *
     * @throws EVCacheException
     */
    @Test
    public void testSetTTLTimestamp() throws EVCacheException {
        final EVCache evCache = Mockito.mock(EVCache.class);
        final String key = "KEY";
        final CachedResponse cachedResponse = Mockito.mock(CachedResponse.class);
        final MemcacheRestCache memcacheRestCache = new MemcacheRestCache(evCache);
        final ArgumentCaptor<Integer> ttlCaptor = ArgumentCaptor.forClass(Integer.class);

        Mockito.when(cachedResponse.getTTL()).thenReturn(2592001L);

        memcacheRestCache.set(key, cachedResponse);

        long now = (System.currentTimeMillis() / 1000L);

        Mockito.verify(evCache)
                        .set(
                            Matchers.eq(key),
                            Matchers.any(),
                            Matchers.any(EVCacheTranscoder.class),
                            ttlCaptor.capture()
                        );

        int capturedTTL = ttlCaptor.getValue();

        Assert.assertTrue(capturedTTL > 2592001L);
        Assert.assertTrue(capturedTTL > now);
        Assert.assertTrue(capturedTTL < now + 2592002L);
    }
    
    
    @After
    public void teardown(){
        daemon.stop();
    }
}
