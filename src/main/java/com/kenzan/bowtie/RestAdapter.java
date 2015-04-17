package com.kenzan.bowtie;

import com.google.common.base.Preconditions;
import com.kenzan.bowtie.annotation.Encoding;
import com.netflix.client.ClientFactory;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RestAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAdapter.class);
    
    private String namedClient;
    private RestAdapterConfig restAdapterConfig;

    private RestAdapter(String namedClient, RestAdapterConfig restAdapterConfig) {
        this.namedClient = namedClient;
        this.restAdapterConfig = restAdapterConfig;
    }

    public static RestAdapter getNamedAdapter(String namedAdapter) {
        return new RestAdapter(namedAdapter, RestAdapterConfig.createDefault());
    }

    public static RestAdapter getNamedAdapter(String namedAdapter, RestAdapterConfig restAdapterConfig) {
        return new RestAdapter(namedAdapter, restAdapterConfig);
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientClass) {

        Preconditions.checkNotNull("NamedClient required", this.namedClient);
        Preconditions.checkNotNull("MessageSerializer required", this.restAdapterConfig.getMessageSerializer());
        
        LOGGER.info("Using NamedClient {}", this.namedClient);
        LOGGER.info("Using MessageSerializer {}", this.restAdapterConfig.getMessageSerializer());
        
        final RestClient restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
        restClient.getJerseyClient().addFilter(new LoggerFilter());
        
        if(this.restAdapterConfig.getEncoding() == Encoding.gzip){
            restClient.getJerseyClient().addFilter(new GZIPContentEncodingFilter());
        }
        
        InvocationHandler invocationHandler = new JerseyInvocationHandler(restClient, this.restAdapterConfig);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
