package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.kenzan.ribbonproxy.annotation.Encoding;
import com.kenzan.ribbonproxy.serializer.MessageSerializer;
import com.netflix.client.ClientFactory;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;


public class RestAdapter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAdapter.class);
    
    private String namedClient;
    private MessageSerializer messageSerializer;
    private Encoding encoding;

    private RestAdapter(String namedClient, MessageSerializer messageSerializer, Encoding encoding) {
        this.namedClient = namedClient;
        this.messageSerializer = messageSerializer;
        this.encoding = encoding;
    }

    public static class Builder{
        
        private String namedClient;
        private MessageSerializer messageSerializer;
        private Encoding encoding = Encoding.none;

        public Builder setNamedClient(String namedClient){
            this.namedClient = namedClient;
            return this;
        }
        
        public Builder setMessageSerializer(MessageSerializer messageSerializer){
            this.messageSerializer = messageSerializer;
            return this;
        }
        
        public Builder setEncoding(Encoding encoding){
            this.encoding = encoding;
            return this;
        }

        public RestAdapter build() {
            return new RestAdapter(namedClient, messageSerializer, encoding);
        }
        
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientClass) {

        Preconditions.checkNotNull("NamedClient required", this.namedClient);
        Preconditions.checkNotNull("MessageSerializer required", this.messageSerializer);
        
        LOGGER.info("Using NamedClient {}", this.namedClient);
        LOGGER.info("Using MessageSerializer {}", this.messageSerializer);
        
        final RestClient restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
        restClient.getJerseyClient().addFilter(new LoggerFilter());
        
        if(this.encoding == Encoding.gzip){
            restClient.getJerseyClient().addFilter(new GZIPContentEncodingFilter());
        }
        
        InvocationHandler invocationHandler = new JerseyInvocationHandler(restClient, this.messageSerializer);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
