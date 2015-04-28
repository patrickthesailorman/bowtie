package com.kenzan.bowtie.http;

import com.kenzan.bowtie.RestAdapterConfig;
import com.kenzan.bowtie.serializer.MessageSerializer;
import com.netflix.niws.client.http.RestClient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/***
 * <p>
 * InvocationHandler to handle the API calls.  Introspects methods during the first call and 
 * caches metadata required to make HTTP calls as MethodInfo objects.
 * </p>
 * 
 * <p>
 * The InvocationHandler then passes the {@link MethodInfo} and the runtime parameters to a generic {@link JerseyHystrixCommand} to execute the HTTP request
 * </p>
 * 
 * <p>
 * Parses the response using a {@link MessageSerializer} depending on the return type parsed from the Method.
 * </p>  
 *  
 *
 */
public class JerseyInvocationHandler implements InvocationHandler{
    
    private final Map<Method, MethodInfo> cache = new ConcurrentHashMap<>();
    private final RestClient restClient;
    private final RestAdapterConfig restAdapterConfig;

    public JerseyInvocationHandler(RestClient restClient, RestAdapterConfig restAdapterConfig) {
        this.restAdapterConfig = restAdapterConfig;
        this.restClient = restClient;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
        System.out.println("\n\n");
        System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
        
        MethodInfo methodInfo = cache.get(method);
        if(methodInfo == null){
            methodInfo = new MethodInfo(method, this.restAdapterConfig);
            cache.put(method, methodInfo);
        }
        
        JerseyHystrixCommand command = new JerseyHystrixCommand(methodInfo, this.restClient, this.restAdapterConfig, args);
        return methodInfo.isObservable() ? command.observe() : command.execute();
    }
}