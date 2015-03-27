package com.kenzan.bowtie.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;

//XXX:  Unit test heavily
public class RestCachingPolicy {
    
    private final List<Integer> CACHEABLE_STATUSES = Arrays.asList(
        200, // OK
        203, // Non-Authoritative Information
        300, // Multiple Choices
        301, // Moved Permanently
        410 // Gone
        );

    private final List<String> UNACCEPTABLE_CACHE_CONTROL_VALUE = Arrays.asList("no-store", "no-cache", "private");

    private final List<String> ACCEPTABLE_CACHE_CONTROL_VALUE = Arrays.asList("max-age", "must-revalidate",
        "proxy-revalidate", "public");

    public boolean isCachable(HttpResponse httpResponse) {
        
        
        // Acceptable to cache if one of the following headers
        if (CACHEABLE_STATUSES.contains(httpResponse.getStatus())) {
            final Optional<String> cacheControl =
                Optional.ofNullable(httpResponse.getHeaders().get("Cache-Control")).map(
                    list -> list.stream().findFirst().orElse(""));
            
            if (cacheControl.isPresent()) {
                // Explicitly cannot cache if the response has an unacceptable
                // cache-control header
                if (UNACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    return false;
                }

                // Explicitly can cache the response has an acceptable
                // cache-control header
                if (ACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    return true;
                }
            }
        }
        
        return false;
    }

    public boolean isCachable(HttpRequest request) {

        
        if(request.getVerb() == Verb.GET){
            
            final Optional<String> cacheControl = 
                            Optional.ofNullable(request.getHeaders().get("Cache-Control"))
                                .map(list -> list.stream().findFirst().orElse(""));
            
            
            if (cacheControl.isPresent()) {
                // Explicitly cannot cache if the response has an unacceptable
                // cache-control header
                if (UNACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    return false;
                }
            }
        }
        
        return true;
    }
}
