package com.netflix.niws.client.http;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;


public class CachedResponseTest {

    private static HashMap<String, Collection<String>> newHeaders(final long maxAge) {
        final HashMap<String, Collection<String>> map = new HashMap<String, Collection<String>>();
        map.put("Cache-Control", Arrays.asList("no-transform,public,max-age=" + maxAge + ",s-maxage=900"));
        return map;
    }

    
    @Test
    public void testCacheControlHundredSeconds() {

        long maxAge = 100;
        final Map<String, Collection<String>> headers = newHeaders(maxAge);
        
        CachedResponse cachedResponse = new CachedResponse(HttpStatus.SC_OK, headers, "{'foo' : 'bar' }".getBytes());
        Assert.assertThat(cachedResponse.getTTL(), IsEqual.equalTo(maxAge));
    }
    
    
    @Test
    public void testCacheControlTwoSeconds() {

        long maxAge = 2;
        final Map<String, Collection<String>> headers = newHeaders(maxAge);
        
        CachedResponse cachedResponse = new CachedResponse(HttpStatus.SC_OK, headers, "{'foo' : 'bar' }".getBytes());
        Assert.assertThat(cachedResponse.getTTL(), IsEqual.equalTo(maxAge));
    }

}
