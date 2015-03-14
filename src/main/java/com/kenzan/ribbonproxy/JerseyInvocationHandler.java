package com.kenzan.ribbonproxy;

import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.map.ObjectMapper;

import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;
import com.kenzan.ribbonproxy.annotation.QueryParam;
import com.netflix.client.ClientFactory;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Builder;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.LoggingFilter;

class JerseyInvocationHandler implements InvocationHandler{
    
    private static class MethodInfo{
        
        private final static VerbAnnotationFunction VERB_ANNOTATION_FUNCTION = new VerbAnnotationFunction();
        
        final private String path;
        private final Parameter[] parameters;
        private final Class responseClass;
        private final Verb verb;

        public MethodInfo(final Method method) {
            
            //Get PATH
            Optional<String> path = Arrays.stream(method.getAnnotationsByType(Path.class))
                            .findFirst()
                            .map(t -> ((Path)t).value());
            
            if(!path.isPresent()){
                throw new IllegalStateException("No Path annotation present.");
            }
            
            this.responseClass = method.getReturnType();
            this.path = path.get();
            this.parameters = method.getParameters();
            
            this.verb = VERB_ANNOTATION_FUNCTION.apply(method.getAnnotations());
            
        }
        
        
        private String getRenderedPath(final Object[] args) {
            
            final List<Object> pathArgs = new ArrayList<>();
            
            for(int i = 0; i < parameters.length; i++){
                final Parameter parameter = parameters[i];
                if(parameter.getAnnotation(PathParam.class) != null){
                    pathArgs.add(args[i]);
                }
            }
            
            String renderedPath = UriBuilder.fromPath(path)
                            .buildFromEncoded(pathArgs.toArray())
                            .toString();
            return renderedPath;

        }
        
        @Override
        public String toString() {
            return "HttpRequest [path=" + path + ", parameters=" + parameters + "]";
        }


        public Map<String,String> getQueryParameters(Object[] args) {
            
            final Map<String, String> paramMap = new HashMap<>();
            
            for(int i = 0; i < parameters.length; i++){
                final QueryParam queryParam = parameters[i].getAnnotation(QueryParam.class);
                if(queryParam != null){
                    paramMap.put(queryParam.value(), String.valueOf(args[i]));
                }
            }
            return paramMap;
        }
    }
    
    private Map<Method, MethodInfo> cache = new HashMap<>();

    private String namedClient;

    public JerseyInvocationHandler(String namedClient) {
        this.namedClient = namedClient;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
        System.out.println("\n\n");
        System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
        System.out.println("namedClient => " + namedClient);
        
        MethodInfo methodInfo = cache.get(method);
        if(methodInfo == null){
            methodInfo = new MethodInfo(method);
            cache.put(method, methodInfo);
        }
        
        RestClient restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
        restClient.getJerseyClient().addFilter(new LoggingFilter(System.out));
        
        final Builder requestBuilder = HttpRequest.newBuilder()
        .verb(methodInfo.verb)
        .uri(methodInfo.getRenderedPath(args));
        
        methodInfo.getQueryParameters(args).forEach((k,v) -> {
            System.out.println(k + " " + v);
            requestBuilder.queryParams(k, v);
        });
        
//        requestBuilder.queryParams("h", "v");
        
        HttpRequest request = requestBuilder.build();
        final HttpResponse httpResponse = restClient.executeWithLoadBalancer(request);
        
        final ObjectMapper objectMapper = new ObjectMapper(); //XXX should this be cached?
        Object object = objectMapper.reader(methodInfo.responseClass).readValue(httpResponse.getInputStream());
        
        return object;
    }   
}