package com.kenzan.ribbonproxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Optional;

import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;


public class RestAdapter {
    
    private String endpoint;

    private static class JerseyInvokationHandler implements InvocationHandler{

        private String endpoint;

        public JerseyInvokationHandler(String endpoint) {
            this.endpoint = endpoint;
            System.out.println("Endpoint: " + endpoint);
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            
            //Get PATH
            Optional<String> path = Arrays.stream(method.getAnnotationsByType(Path.class))
                            .findFirst()
                            .map(t -> ((Path)t).value());
            
            if(path.isPresent()){
                System.out.println(path.get());
            }
            
            
            
            System.out.println("\n\n");
            System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
            System.out.println("endpoint => " + endpoint);
            System.out.println("path => " + path);
            
            
            final Parameter[] parameters = method.getParameters();
            for(int i=0; i < parameters.length; i++){
                
                Parameter parameter = parameters[i];
                Optional<PathParam> pathParam = Arrays.stream(parameter.getAnnotationsByType(PathParam.class)).findFirst();
                if(pathParam.isPresent()){
                    System.out.println(pathParam.get().value() + " => " + args[i]);
                }
            }
            
            return method.getReturnType().newInstance();
        }   
    }

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

        InvocationHandler invocationHandler = new JerseyInvokationHandler(this.endpoint);
        Object proxyInstance = Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(),
            new Class<?>[]{clientClass}, invocationHandler);
        return (T)proxyInstance;
    }
}
