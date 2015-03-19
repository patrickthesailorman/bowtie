/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;

public class RestCachingPolicy {

    public String calculateCacheKey(HttpRequest httpRequest) {
        return httpRequest.getUri().toString();
    }

    public boolean isCachable(HttpResponse httpResponse) {
        return true;
    }
}
