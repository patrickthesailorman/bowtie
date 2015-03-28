package com.kenzan.bowtie.cache;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;

public class RestCachingPolicy {
    
    private static final Logger LOGGER  = LoggerFactory.getLogger(RestCachingPolicy.class);
    
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
        
        LOGGER.debug("HttpStatus: {}", httpResponse.getStatus());
        
        // Acceptable to cache if one of the following headers
        if (CACHEABLE_STATUSES.contains(httpResponse.getStatus())) {
            final Optional<String> cacheControl =
                Optional.ofNullable(httpResponse.getHeaders().get("Cache-Control")).map(
                    list -> list.stream().findFirst().orElse(""));
            
            
            LOGGER.debug("Found Cache-Control header: {}", cacheControl.isPresent());
            if (cacheControl.isPresent()) {
                // Explicitly cannot cache if the response has an unacceptable
                // cache-control header
                if (UNACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    
                    LOGGER.debug("Found UNACCEPTABLE_CACHE_CONTROL_VALUE in response");
                    return false;
                }

                // Explicitly can cache the response has an acceptable
                // cache-control header
                if (ACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    
                    LOGGER.debug("Found ACCEPTABLE_CACHE_CONTROL_VALUE in response");
                    return true;
                }
            }
        }
        
        return false;
    }

    public boolean isCachable(HttpRequest request) {

        boolean isCacheable = true;
        
        LOGGER.debug("Verb: {}", request.getVerb());
        if(request.getVerb() == Verb.GET){
            
            final Optional<String> cacheControl = 
                            Optional.ofNullable(request.getHeaders().get("Cache-Control"))
                                .map(list -> list.stream().findFirst().orElse(""));
            
            LOGGER.debug("Found Cache-Control header: {}", cacheControl.isPresent());
            if (cacheControl.isPresent()) {
                // Explicitly cannot cache if the response has an unacceptable
                // cache-control header
                if (UNACCEPTABLE_CACHE_CONTROL_VALUE.stream().anyMatch(a -> cacheControl.get().contains(a))) {
                    LOGGER.debug("Found UNACCEPTABLE_CACHE_CONTROL_VALUE in request");
                    
                    isCacheable = false;
                }
            }
        }
        
        LOGGER.debug("Request isCacheable: {}", isCacheable);
        return isCacheable;
    }
}
