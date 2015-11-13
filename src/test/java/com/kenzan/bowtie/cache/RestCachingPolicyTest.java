/**
 * Copyright (C) 2015 Kenzan (labs@kenzan.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
