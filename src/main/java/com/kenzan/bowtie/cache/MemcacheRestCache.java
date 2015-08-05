package com.kenzan.bowtie.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

import net.spy.memcached.CachedData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.netflix.evcache.EVCache;
import com.netflix.evcache.EVCacheException;
import com.netflix.evcache.EVCacheTranscoder;
import com.netflix.niws.client.http.CachedResponse;

/***
 * <p>
 * {@link RestCache} implemented using Memcache.  Uses Kyro for serialization.  Honors the TTL of the value.  
 * </p> 
 * 
 * 
 * XXX rename to evcache
 */
public class MemcacheRestCache  implements RestCache{

    private static final long THIRTY_DAYS = 2592000L;

    protected static class MemcacheRestCacheException extends RuntimeException{

        public MemcacheRestCacheException(String message, Throwable throwable) {
            super(message, throwable);
            
            LOGGER.error(message, throwable);
        }

        private static final long serialVersionUID = -7775657351842400056L;
        
    }
    
    private static class CachedDataResponseEVCacheTranscoder implements EVCacheTranscoder<CachedResponse>{
        
        private static final int CACHED_DATA_OBJECT_FLAG = 800;
        private Kryo kryo;

        public CachedDataResponseEVCacheTranscoder() {
            kryo = new Kryo();
            kryo.register(CachedResponse.class);
        }
        
        
        @Override
        public boolean asyncDecode(CachedData d) {
            return false;
        }

        @Override
        public CachedData encode(CachedResponse o) {
            
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 final Output output = new Output(baos);){
                 kryo.writeObject(output, o);
                
                 return new CachedData(CACHED_DATA_OBJECT_FLAG, output.getBuffer(), getMaxSize());
            } catch (IOException e) {
                throw new MemcacheRestCacheException("Could not encode " + o.getClass().getName(), e);
            }
        }

        @Override
        public CachedResponse decode(CachedData d) {
            
            try (final ByteArrayInputStream bais = new ByteArrayInputStream(d.getData());
                 final Input input = new Input(bais)){
                
                return kryo.readObject(input, CachedResponse.class);
            } catch (IOException e) {
                throw new MemcacheRestCacheException("Could not decode " + CachedResponse.class.getName(), e);
            }
        }

        @Override
        public int getMaxSize() {
            return CachedData.MAX_SIZE;
        }
    }
    
    
    final static protected CachedDataResponseEVCacheTranscoder TRANSCODER = new CachedDataResponseEVCacheTranscoder();
    
    final static private Logger LOGGER = LoggerFactory.getLogger(MemcacheRestCache.class);
    
    private final EVCache evCache;
    
    
    public MemcacheRestCache(EVCache evCache) {
        this.evCache = evCache;
    }

    @Override
    public Optional<CachedResponse> get(String key) {
        LOGGER.debug("Getting key from cache: {}", key);

        try {
            return Optional.ofNullable(evCache.get(key, TRANSCODER));
        } catch (EVCacheException e) {
            throw new MemcacheRestCacheException("Could get key " + key, e);
        }   
    }

    @Override
    public void set(String key, CachedResponse value) {
        LOGGER.debug("Setting cache: {} for {}",  key, value.getTTL());
        try {
            long ttl = value.getTTL();

            //Convert TTL to unix timestamp if time is greater than 30 days.
            if(ttl >= THIRTY_DAYS){
                ttl += (System.currentTimeMillis() / 1000);
            }

            if(ttl > 0){
                evCache.set(key, value, TRANSCODER, (int)ttl);
            }
        } catch (EVCacheException e) {
            throw new MemcacheRestCacheException("Could set key " + key, e);
        }
    }
}
