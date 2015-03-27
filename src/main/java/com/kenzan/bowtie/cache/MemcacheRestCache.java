package com.kenzan.bowtie.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Optional;

import net.spy.memcached.CachedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.evcache.EVCache;
import com.netflix.evcache.EVCacheException;
import com.netflix.evcache.EVCacheTranscoder;
import com.netflix.evcache.pool.EVCacheClientPoolManager;
import com.netflix.niws.client.http.CachedResponse;

public class MemcacheRestCache  implements RestCache{
    
    private static class CachedDataResponseEVCacheTranscoder implements EVCacheTranscoder<CachedResponse>{
        
        private static final int CACHED_DATA_OBJECT_FLAG = 800;

        @Override
        public boolean asyncDecode(CachedData d) {
            return false;
        }

        @Override
        public CachedData encode(CachedResponse o) {
            
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 final ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);){
                objectOutputStream.writeObject(o);
                
                return new CachedData(CACHED_DATA_OBJECT_FLAG, baos.toByteArray(), getMaxSize());
            } catch (IOException e) {
                e.printStackTrace();  //XXX better
            }
            
            return null;  //XXX better
        }

        @Override
        public CachedResponse decode(CachedData d) {
            
            try (final ByteArrayInputStream bais = new ByteArrayInputStream(d.getData());
                 final ObjectInputStream objectInputStream = new ObjectInputStream(bais)){
                
                return (CachedResponse) objectInputStream.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();  //XXX better
            }
            
            return null;  //XXX better
        }

        @Override
        public int getMaxSize() {
            return CachedData.MAX_SIZE;
        }
    }
    
    
    final static private CachedDataResponseEVCacheTranscoder TRANSCODER = new CachedDataResponseEVCacheTranscoder();
    
    final static private Logger LOGGER = LoggerFactory.getLogger(MemcacheRestCache.class);
    
    private final EVCache evCache;
    
    
    public MemcacheRestCache() {
        EVCacheClientPoolManager.getInstance().initEVCache("SAMPLECACHE");
        
        evCache = (new EVCache.Builder())
                        .setAppName("SAMPLECACHE")
                        .setCacheName("cid")
                        .enableZoneFallback()
                        .build();

    }

    @Override
    public Optional<CachedResponse> get(String key) {
        LOGGER.debug("Getting cache: " + key);

        try {
            return Optional.ofNullable(evCache.get(key, TRANSCODER));
        } catch (EVCacheException e) {
            e.printStackTrace(); //XXX Handle better
        }   
        
        return Optional.empty();
    }

    @Override
    public void set(String key, CachedResponse value) {
        LOGGER.debug("Setting cache: {}",  key);
        try {
            evCache.set(key, value);
        } catch (EVCacheException e) {
            e.printStackTrace();  //XXX Handle better
        }
    }
}
