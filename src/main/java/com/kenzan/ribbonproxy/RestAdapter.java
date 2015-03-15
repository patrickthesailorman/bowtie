package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


public class RestAdapter {
    
    private String namedClient;

    private RestAdapter(String namedClient) {
        this.namedClient = namedClient;
    }

    public static class Builder{
        
        private String namedClient;

        public Builder setNamedClient(String namedClient){
            this.namedClient = namedClient;
            return this;
        }

        public RestAdapter build() {
            return new RestAdapter(namedClient);
        }
        
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> clientClass) {

        InvocationHandler invocationHandler = new JerseyInvocationHandler(this.namedClient);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
