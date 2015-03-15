package com.kenzan.ribbonproxy;

import java.io.InputStream;
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
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.map.ObjectMapper;

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.GET;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.POST;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.netflix.client.ClientFactory;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Builder;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.LoggingFilter;

class JerseyInvocationHandler implements InvocationHandler{
    
    private static class MethodInfo{
        
        final private String path;
        private final Parameter[] parameters;
        private final Class responseClass;
        private final Verb verb;
        private final Map<String, String> headers;

        public MethodInfo(final Method method) {
            
            //GET VERB ANNOTATION
            Optional<Annotation> verbAnnotation = Arrays.stream(method.getAnnotations())
                            .filter(t ->
                                GET.class.equals(t.annotationType()) ||
                                POST.class.equals(t.annotationType())
                            ).findFirst();
            
            
            if(!verbAnnotation.isPresent()){
                throw new IllegalStateException("No Path annotation present.");
            }
            
            final Optional<Verb> httpVerb = verbAnnotation.map(t -> {
                
                final Verb verb;
                if(GET.class.equals(t.annotationType())){
                    verb = Verb.GET;
                }else if(POST.class.equals(t.annotationType())){
                    verb = Verb.POST;
                }else{
                    throw new IllegalStateException("Unsupported verb annotation: " + t);
                }
                
                return verb;
            });
                
            final Optional<String> httpPath = verbAnnotation.map(t -> {
                
                final String path;
                if(GET.class.equals(t.annotationType())){
                    path = ((GET)t).value();
                }else if(POST.class.equals(t.annotationType())){
                    path = ((POST)t).value();
                }else{
                    throw new IllegalStateException("Unsupported path annotation: " + t);
                }
                
                return path;
            });
            
            this.path = httpPath.get();
            this.verb = httpVerb.get();
            
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
            
            String renderedPath = UriBuilder.fromPath(path)
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
        
        @Override
        public String toString() {
            return "HttpRequest [path=" + path + ", parameters=" + parameters + "]";
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
    
    private Map<Method, MethodInfo> cache = new HashMap<>();

    private String namedClient;

    public JerseyInvocationHandler(String namedClient) {
        this.namedClient = namedClient;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
   
        System.out.println("\n\n");
        System.out.println("args: " + method.getName() +  " " + (args == null ? "" : Arrays.asList(args)));
        System.out.println("namedClient => " + namedClient);
        
        final ObjectMapper objectMapper = new ObjectMapper(); //XXX should this be cached?
        
        MethodInfo methodInfo = cache.get(method);
        if(methodInfo == null){
            methodInfo = new MethodInfo(method);
            cache.put(method, methodInfo);
        }
        
        final RestClient restClient = (RestClient)ClientFactory.getNamedClient(namedClient);
        restClient.getJerseyClient().addFilter(new LoggingFilter(System.out));  //XXX change to logger
        
        final Builder requestBuilder = HttpRequest.newBuilder()
        .verb(methodInfo.verb)
        .uri(methodInfo.getRenderedPath(args));
        
        methodInfo.getQueryParameters(args).forEach((k,v) -> {
            requestBuilder.queryParams(k, v);
        });
        
        methodInfo.getHeaders(args).forEach((k,v) -> {
            requestBuilder.header(k, v);
        });
        
        methodInfo.getBody(args).ifPresent(t -> {
            try {
                requestBuilder
                .entity(objectMapper.writeValueAsString(t));
            } catch (Exception e) {
                e.printStackTrace();  ///XXX: decide there is better exception handling
            }
        });
        
        HttpRequest request = requestBuilder.build();
        final HttpResponse httpResponse = restClient.executeWithLoadBalancer(request);
        
        final Object object;
        if(HttpResponse.class.equals(methodInfo.responseClass)){
            object = httpResponse;
        }else{
            object = objectMapper.reader(methodInfo.responseClass).readValue(httpResponse.getInputStream());
        }
        
        return object;
    }   
}