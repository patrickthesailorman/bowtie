package com.kenzan.ribbonproxy;

import java.lang.annotation.Annotation;
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

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.Http;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.netflix.client.ClientFactory;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Builder;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.LoggingFilter;

class JerseyInvocationHandler implements InvocationHandler{
    
    
    private class JerseyHystrixCommand extends HystrixCommand<Object>{

        private final MethodInfo methodInfo;
        private final Object[] args;

        public JerseyHystrixCommand(final String groupKey, final String commandKey, final MethodInfo methodInfo, final Object[] args) {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(groupKey))
                .andCommandKey(HystrixCommandKey.Factory.asKey(commandKey)));
            this.methodInfo = methodInfo;
            this.args = args;
        }
        
        @Override
        protected Object run() throws Exception {

            restClient.getJerseyClient()
                .addFilter(new LoggingFilter(System.out));  //XXX change to logger make configurable from the adapter?

            final HttpRequest request = methodInfo.toHttpRequest(args);
            final HttpResponse httpResponse = restClient.executeWithLoadBalancer(request);

            final Object object;
            if (HttpResponse.class.equals(methodInfo.responseClass)) {
                object = httpResponse;
            } else {
                object = objectMapper.reader(methodInfo.responseClass).readValue(httpResponse.getInputStream());
            }

            return object;
        }
        
    }
    
    private class MethodInfo{
        
        private final Parameter[] parameters;
        private final Class responseClass;
        private final Map<String, String> headers;
        private final Http http;

        public MethodInfo(final Method method) {
            
            //GET VERB ANNOTATION
            http = Arrays.stream(method.getAnnotations())
                            .filter(a -> Http.class.equals(a.annotationType()))
                            .map(a -> (Http)a)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No Http annotation present."));
            
            this.responseClass = method.getReturnType();
            this.parameters = method.getParameters();
            
            headers = Arrays.stream(
                method.getAnnotationsByType(Header.class))
                .collect(Collectors.toMap(
                    t -> t.name(), 
                    t -> t.value()
                ));
            
        }
        
        
        private String getRenderedPath(final Object[] args) {
            
            final List<Object> pathArgs = new ArrayList<>();
            
            for(int i = 0; i < parameters.length; i++){
                final Parameter parameter = parameters[i];
                if(parameter.getAnnotation(Path.class) != null){
                    pathArgs.add(args[i]);
                }
            }
            
            String renderedPath = UriBuilder.fromPath(http.uriTemplate())
                            .buildFromEncoded(pathArgs.toArray())
                            .toString();
            return renderedPath;

        }
        
        private Map<String, String> getHeaders(final Object[] args){
            Map<String, String> allHeaders = this.headers;
            
            for(int i = 0; i < parameters.length; i++){
                final Parameter parameter = parameters[i];
                Header annotation = parameter.getAnnotation(Header.class);
                if(annotation != null){
                    allHeaders.put(annotation.name(), String.valueOf(args[i]));
                }
            }
            
            return allHeaders;
        }
        
        private Optional<Object> getBody(Object[] args){
            
            Object body = null;
            for(int i = 0; i < parameters.length; i++){
                final Parameter parameter = parameters[i];
                Body annotation = parameter.getAnnotation(Body.class);
                if(annotation != null){
                    body = args[i];
                }
            }
            
            return Optional.ofNullable(body);
        }
        
        
        private HttpRequest toHttpRequest(Object[] args) {

            final Builder requestBuilder = HttpRequest.newBuilder()
            .verb(this.http.method())
            .uri(this.getRenderedPath(args));
            
            this.getQueryParameters(args).forEach((k,v) -> {
                requestBuilder.queryParams(k, v);
            });
            
            this.getHeaders(args).forEach((k,v) -> {
                requestBuilder.header(k, v);
            });
            
            this.getBody(args).ifPresent(t -> {
                try {
                    requestBuilder
                    .entity(objectMapper.writeValueAsString(t));
                } catch (Exception e) {
                    e.printStackTrace();  ///XXX: decide there is better exception handling
                }
            });
            
            HttpRequest request = requestBuilder.build();
            return request;
        }

        public Map<String,String> getQueryParameters(Object[] args) {
            
            final Map<String, String> paramMap = new HashMap<>();
            
            for(int i = 0; i < parameters.length; i++){
                final Query queryParam = parameters[i].getAnnotation(Query.class);
                if(queryParam != null){
                    paramMap.put(queryParam.value(), String.valueOf(args[i]));
                }
            }
            return paramMap;
        }
    }
    
    final ObjectMapper objectMapper = new ObjectMapper(); //XXX Is this thread safe?
    private Map<Method, MethodInfo> cache = new HashMap<>();
    private final RestClient restClient;


    public JerseyInvocationHandler(String namedClient) {
        restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
        System.out.println("\n\n");
        System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
        
        MethodInfo methodInfo = cache.get(method);
        if(methodInfo == null){
            methodInfo = new MethodInfo(method);
            cache.put(method, methodInfo);
        }
        
        return new JerseyHystrixCommand("GroupKey", "CommandKey", methodInfo, args).execute();
    }
}