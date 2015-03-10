package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


public class RestAdapter {
    
    private String endpoint;

    private RestAdapter(String endpoint) {
        this.endpoint = endpoint;
    }

    public static class Builder{
        
        private String endpoint;

        public Builder setEndpoint(String endpoint){
            this.endpoint = endpoint;
            return this;
        }

        public RestAdapter build() {
            return new RestAdapter(endpoint);
        }
        
    }

    public <T> T create(Class<T> clientClass) {

        InvocationHandler invocationHandler = new JerseyInvocationHandler(this.endpoint);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
