package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

import com.google.common.base.Preconditions;
import com.kenzan.ribbonproxy.serializer.MessageSerializer;


public class RestAdapter {
    
    private String namedClient;
    private MessageSerializer messageSerializer;

    private RestAdapter(String namedClient, MessageSerializer messageSerializer2) {
        this.namedClient = namedClient;
        messageSerializer = messageSerializer2;
    }

    public static class Builder{
        
        private String namedClient;
        private MessageSerializer messageSerializer;

        public Builder setNamedClient(String namedClient){
            this.namedClient = namedClient;
            return this;
        }
        
        public Builder setMessageSerializer(MessageSerializer messageSerializer){
            this.messageSerializer = messageSerializer;
            return this;
        }

        public RestAdapter build() {
            return new RestAdapter(namedClient, messageSerializer);
        }
        
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientClass) {

        Preconditions.checkNotNull("NamedClient required", this.namedClient);
        Preconditions.checkNotNull("MessageSerializer required", this.messageSerializer);
        
        InvocationHandler invocationHandler = new JerseyInvocationHandler(this.namedClient, this.messageSerializer);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
