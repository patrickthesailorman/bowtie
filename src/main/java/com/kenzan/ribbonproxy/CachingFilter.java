package com.kenzan.ribbonproxy;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;


public class CachingFilter extends ClientFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CachingFilter.class);
    
    private final CacheStore cacheStore;
    
    public CachingFilter(CacheStore cacheStore) {
        this.cacheStore = cacheStore;
    }
    
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

        String cacheKey = createCacheKey(request);
        
        Optional<String> responseBody = cacheStore.get(cacheKey);
        if(responseBody.isPresent()){
            return createResponse(responseBody.get());
        }
      
        ClientResponse response = getNext().handle(request);
        
        //evaluate headers
        if(ifCacheable(response)){
            cacheStore.put(cacheKey, createCacheValue(response));
        }
        
        return response;
    }

    private String createCacheValue(ClientResponse response) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean ifCacheable(ClientResponse response) {
        // TODO Auto-generated method stub
        return false;
    }

    private ClientResponse createResponse(String string) {
        // TODO Auto-generated method stub
        return null;
    }

    private String createCacheKey(ClientRequest request) {
        return request.getURI().toString();
    }

}
