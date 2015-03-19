/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.ribbonproxy.cache;

import com.netflix.client.http.HttpResponse;

public class RestCachingPolicy {

    public boolean isCachable(HttpResponse httpResponse) {
        // TODO: determine policy for a cachable response
        return true;
    }
}
