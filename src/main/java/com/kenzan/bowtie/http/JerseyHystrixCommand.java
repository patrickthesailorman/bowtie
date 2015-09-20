package com.kenzan.bowtie.http;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;

import com.google.common.io.ByteStreams;
import com.kenzan.bowtie.RestAdapterConfig;
import com.kenzan.bowtie.cache.RestCache;
import com.kenzan.bowtie.cache.RestCachingPolicy;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.niws.client.http.CachedResponse;
import com.netflix.niws.client.http.RestClient;

public class JerseyHystrixCommand extends HystrixCommand<Object> {

    private final MethodInfo methodInfo;
    private final RestClient restClient;
    private final RestAdapterConfig restAdapterConfig;
    private final Object[] args;

    public JerseyHystrixCommand(final MethodInfo methodInfo,
            final RestClient client, final RestAdapterConfig config,
            final Object[] args) {
        super(methodInfo.getSetter());
        this.methodInfo = methodInfo;
        this.restClient = client;
        this.restAdapterConfig = config;
        this.args = args;
    }

    @Override
    protected Object run() throws Exception {

        final HttpRequest request = methodInfo.toHttpRequest(args);
        final String cacheKey = methodInfo.getCacheKey(args);

        final Optional<RestCache> cache = Optional.ofNullable(restAdapterConfig
                .getRestCache());
        final RestCachingPolicy cachingPolicy = new RestCachingPolicy();

        final boolean isRequestCacheable = cache.isPresent()
                && cachingPolicy.isCachable(request);
        if (isRequestCacheable) {

            final Optional<CachedResponse> cachedResponse = cache.get().get(
                    cacheKey);

            if (cachedResponse.isPresent()) {
                // XXX Check to see if need to convert to a HttpResponse
                if (HttpResponse.class.equals(methodInfo.getResponseClass())) {
                    return cachedResponse.get().toHttpResponse(
                            restClient.getJerseyClient()
                                    .getMessageBodyWorkers());
                }
                return restAdapterConfig.getMessageSerializer().readValue(
                        methodInfo.getResponseClass(),
                        new ByteArrayInputStream(cachedResponse.get()
                                .getCachedBytes()));
            }
        }

        try (final HttpResponse httpResponse = restClient
                .executeWithLoadBalancer(request)) {

            final Object object;
            if (HttpResponse.class.equals(methodInfo.getResponseClass())) {
                object = httpResponse;

            } else {

                // XXX: Need to determine how to handle errors
                final byte[] cachedBytes;
                final InputStream inputStream;

                if (isRequestCacheable
                        && cachingPolicy.isCachable(httpResponse)) {

                    cachedBytes = ByteStreams.toByteArray(httpResponse
                            .getInputStream());

                    cache.get().set(
                            cacheKey,
                            CachedResponse.createResponse(
                                    httpResponse.getStatus(),
                                    httpResponse.getHeaders(), cachedBytes));

                    inputStream = new ByteArrayInputStream(cachedBytes);
                } else {
                    inputStream = httpResponse.getInputStream();
                }

                object = restAdapterConfig.getMessageSerializer().readValue(
                        methodInfo.getResponseClass(), inputStream);
            }

            return object;
        }
    }

}