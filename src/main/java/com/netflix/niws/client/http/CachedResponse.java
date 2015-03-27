package com.netflix.niws.client.http;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.netflix.client.http.HttpResponse;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.InBoundHeaders;
import com.sun.jersey.spi.MessageBodyWorkers;


public class CachedResponse implements Serializable{

    private static final long serialVersionUID = -6367516151816285192L;


    public static CachedResponse createResponse(int status,
                                                  Map<String, Collection<String>> headers,
                                                  byte[] cachedBytes) {
        
        return new CachedResponse(status, headers, cachedBytes);
    }
    
    final private Map<String, Collection<String>> headers;
    final private int status;
    final private byte[] cachedBytes;

    
    public CachedResponse(int status, Map<String, Collection<String>> headers,
        byte[] cachedBytes) {
            
        this.status = status;
        this.headers = headers;
        this.cachedBytes = cachedBytes;
        
    }
    
    public HttpResponse toHttpResponse(MessageBodyWorkers workers){
        
        final InBoundHeaders inBoundHeaders = new InBoundHeaders();
        headers.forEach((k,v) -> {
            inBoundHeaders.put(k, new ArrayList<String>(v));
        });
        
        return new HttpClientResponse(new ClientResponse(status, inBoundHeaders, new ByteArrayInputStream(cachedBytes), workers));
    }

    
    public byte[] getCachedBytes() {
        return cachedBytes;
    }

    public Map<String, Collection<String>> getHeaders() {
        return headers;
    }

    public int getStatus() {
        return status;
    }
}