/*
 * Copyright 2014, Charter Communications, All rights reserved.
 */
package com.kenzan.bowtie.cache;

import java.util.Collection;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.client.http.HttpRequest.Verb;

//XXX:  Unit test heavily
public class RestCachingPolicy {

    public boolean isCachable(HttpResponse httpResponse) {
        // TODO: determine policy for a cachable response
        return true;
    }

    public boolean isCachable(HttpRequest request) {

        boolean isCacheable = false;
        
        if(request.getVerb() == Verb.GET){
            
            
            //XXX Need to review and make better
            Collection<String> cacheHeaders = request.getHeaders().get("Cache-Control");
            
            if(cacheHeaders == null || !cacheHeaders.contains("no-cache")){
                isCacheable = true;
            }
        }
        return isCacheable;
    }
}
