package com.kenzan.bowtie.cache;

import java.util.Collection;
import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.niws.client.http.CachedResponse;

/***
 * <p>
 * Tests {@link RestCachingPolicy}
 * </p>
 * 
 */
public class RestCachingPolicyTest {

    private HashMap<String, Collection<String>> createCacheHeaders() {

        HashMap<String, Collection<String>> map = new HashMap<>();
        map.put("Cache-Control", Lists.newArrayList("max-age=3600, must-revalidate"));
        return map;
    }

    final RestCachingPolicy policy = new RestCachingPolicy();
    
    
    @Test
    public void testIsCachableHttpRequest() {
        HttpRequest cacheableRequest = HttpRequest.newBuilder()
                        .uri("/url")
                        .build();
        Assert.assertThat(policy.isCachable(cacheableRequest), IsEqual.equalTo(Boolean.TRUE));
        
        
        HttpRequest notCacheableRequest = HttpRequest.newBuilder()
                        .uri("/url")
                        .header("Cache-Control", "no-cache")
                        .build();
        Assert.assertThat(policy.isCachable(notCacheableRequest), IsEqual.equalTo(Boolean.FALSE));
    }

    @Test
    public void testIsCachableHttpResponse() {

        
        final HttpResponse notCacheableResponse = new CachedResponse(
            HttpStatus.SC_OK, 
            new HashMap<>(),
            "foo".getBytes()).toHttpResponse(null);
        Assert.assertThat(policy.isCachable(notCacheableResponse), IsEqual.equalTo(Boolean.FALSE));
        
        final HttpResponse cacheableResponse = new CachedResponse(
            HttpStatus.SC_OK, 
            createCacheHeaders(),
            "foo".getBytes()).toHttpResponse(null);
        Assert.assertThat(policy.isCachable(cacheableResponse), IsEqual.equalTo(Boolean.TRUE));
    }

}
