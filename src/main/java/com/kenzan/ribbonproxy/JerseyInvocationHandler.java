package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.kenzan.ribbonproxy.annotation.FormParam;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;

class JerseyInvocationHandler implements InvocationHandler{
    
    private static class MethodInfo{
        final private String path;
        private final Parameter[] parameters;

        public MethodInfo(final Method method) {
            
            //Get PATH
            Optional<String> path = Arrays.stream(method.getAnnotationsByType(Path.class))
                            .findFirst()
                            .map(t -> ((Path)t).value());
            
            if(!path.isPresent()){
                throw new IllegalStateException("No Path annotation present.");
            }
            
            this.path = path.get();
            this.parameters = method.getParameters();
            
        }
        
        @Override
        public String toString() {
            return "HttpRequest [path=" + path + ", parameters=" + parameters + "]";
        }
    }
    
    private Map<Method, MethodInfo> cache = new HashMap<>();

    private String endpoint;

    public JerseyInvocationHandler(String endpoint) {
        this.endpoint = endpoint;
        System.out.println("Endpoint: " + endpoint);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
        System.out.println("\n\n");
        System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
        System.out.println("endpoint => " + endpoint);
        
        MethodInfo methodInfo = cache.get(method);
        if(methodInfo == null){
            methodInfo = new MethodInfo(method);
            cache.put(method, methodInfo);
        }
        
        System.out.println("methodInfo => " + methodInfo);
        
        return method.getReturnType().newInstance();
    }   
}