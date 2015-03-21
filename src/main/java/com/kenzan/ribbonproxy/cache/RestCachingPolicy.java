/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;

public class RestCachingPolicy {

    public boolean isCachable(HttpResponse httpResponse) {
        // TODO: determine policy for a cachable response
        return true;
    }

    public boolean isCachable(HttpRequest request) {

        return true;
    }
}
