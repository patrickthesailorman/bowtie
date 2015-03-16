package com.kenzan.ribbonproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.codehaus.jackson.map.ObjectMapper;

import rx.Observable;

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.Cookie;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.Http;
import com.kenzan.ribbonproxy.annotation.Hystrix;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.kenzan.ribbonproxy.serializer.MessageSerializer;
import com.netflix.client.ClientFactory;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Builder;
import com.netflix.client.http.HttpResponse;
import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.niws.client.http.RestClient;
import com.sun.jersey.api.client.filter.LoggingFilter;

class JerseyInvocationHandler implements InvocationHandler{
    
    private class JerseyHystrixCommand extends HystrixCommand<Object>{

        private final MethodInfo methodInfo;
        private final Object[] args;

        public JerseyHystrixCommand(final MethodInfo methodInfo, final Object[] args) {
            super(methodInfo.setter);
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
                object = messageSerializer.readValue(methodInfo.responseClass, httpResponse.getInputStream());
            }

            return object;
        }
        
    }
    
    private class MethodInfo{

        private final Setter setter;
        private final Parameter[] parameters;
        private final Class<?> responseClass;
        private final Map<String, String> headers;
        private final Http http;
        private final Hystrix hystrix;
        private final boolean isObservable;
        private List<String> cookies = new ArrayList<>();

        public MethodInfo(final Method method) {
            
            //GET HTTP ANNOTATION
            http = Arrays.stream(method.getAnnotations())
                            .filter(a -> Http.class.equals(a.annotationType()))
                            .map(a -> (Http)a)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No Http annotation present."));
            
            //GET HYSTRIX ANNOTATION
            hystrix = Arrays.stream(method.getAnnotations())
                            .filter(a -> Hystrix.class.equals(a.annotationType()))
                            .map(a -> (Hystrix)a)
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("No Hystrix annotation present."));
            
            this.setter = Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(hystrix.groupKey()))
                            .andCommandKey(HystrixCommandKey.Factory.asKey(hystrix.commandKey()));
            
            this.parameters = method.getParameters();
            
            this.headers = Arrays.stream(http.headers())
                .collect(Collectors.toMap(
                    t -> t.name(), 
                    t -> t.value()
                ));

            if(headers.containsKey("Cookie")){
                cookies.add(this.headers.remove("Cookie"));
            }
            
            cookies.addAll(Arrays.stream(http.cookies())
                .map(cookie -> cookie.name() + "=" + cookie.value())
                .collect(Collectors.toList()));
            
            
            final Class returnType = method.getReturnType();
            final Class httpClass = Class.class.equals(http.responseClass()) ? null : http.responseClass();
            
            this.isObservable = Observable.class.equals(returnType);
            if(this.isObservable){
                this.responseClass = Optional.ofNullable(httpClass).orElseThrow(() ->
                        new IllegalStateException("Http responseClass is required for observables"));
            }else{
                this.responseClass = Optional.ofNullable(httpClass).orElse(returnType);
            }
        }
        
        private String getRenderedPath(final Object[] args) {
            
            final Map<String, Object> map = new HashMap<>();
            
            for(int i = 0; i < parameters.length; i++){
                final int count = i;
                Optional.ofNullable(parameters[count].getAnnotation(Path.class))
                .ifPresent(p -> {
                    map.put(p.value(), args[count]);
                });
            }
            
            String renderedPath = UriBuilder.fromPath(http.uriTemplate())
                            .buildFromEncodedMap(map)
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
            
            
            //Cookies
            List<String> requestCookies = new ArrayList<>();
            requestCookies.addAll(cookies);
            
            requestCookies.addAll(this.getCookies(args).entrySet().stream().map(entry -> 
              entry.getKey() + "=" + entry.getValue()  
            ).collect(Collectors.toList()));
            
            if(!requestCookies.isEmpty()){
                requestBuilder.header("Cookie", requestCookies.stream().collect(Collectors.joining(";")));
            }
            
            
            //Body
            this.getBody(args).ifPresent(t -> {
                try {
                    requestBuilder
                    .entity(messageSerializer.writeValue(t));
                } catch (Exception e) {
                    e.printStackTrace();  ///XXX: decide there is better exception handling
                }
            });
            
            HttpRequest request = requestBuilder.build();
            return request;
        }
        
        private Map<String,String> getQueryParameters(Object[] args) {
            
            final Map<String, String> paramMap = new HashMap<>();
            
            for(int i = 0; i < parameters.length; i++){
                final Query queryParam = parameters[i].getAnnotation(Query.class);
                if(queryParam != null){
                    paramMap.put(queryParam.value(), String.valueOf(args[i]));
                }
            }
            return paramMap;
        }

        private Map<String, String> getCookies(final Object[] args){
            Map<String, String> allHeaders = this.headers;
            
            for(int i = 0; i < parameters.length; i++){
                final Parameter parameter = parameters[i];
                Cookie annotation = parameter.getAnnotation(Cookie.class);
                if(annotation != null){
                    allHeaders.put(annotation.name(), String.valueOf(args[i]));
                }
            }
            
            return allHeaders;
        }
    }
    
    private final ObjectMapper objectMapper = new ObjectMapper(); //XXX Is this thread safe?
    private final Map<Method, MethodInfo> cache = new ConcurrentHashMap<>();
    private final RestClient restClient;
    private final MessageSerializer messageSerializer;

    public JerseyInvocationHandler(String namedClient, MessageSerializer messageSerializer) {
        this.messageSerializer = messageSerializer;
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
        
        JerseyHystrixCommand command = new JerseyHystrixCommand(methodInfo, args);
        return methodInfo.isObservable ? command.observe() : command.execute();
    }
}