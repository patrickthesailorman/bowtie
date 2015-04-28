package com.kenzan.bowtie.http;

import com.kenzan.bowtie.RestAdapterConfig;
import com.kenzan.bowtie.annotation.Body;
import com.kenzan.bowtie.annotation.CacheKeyGroup;
import com.kenzan.bowtie.annotation.Cookie;
import com.kenzan.bowtie.annotation.Header;
import com.kenzan.bowtie.annotation.Http;
import com.kenzan.bowtie.annotation.Hystrix;
import com.kenzan.bowtie.annotation.Path;
import com.kenzan.bowtie.annotation.Query;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpRequest.Builder;
import com.netflix.hystrix.HystrixCommand.Setter;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;

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

import rx.Observable;


public class MethodInfo{

    private final Setter setter;
    private final Parameter[] parameters;
    private final Class<?> responseClass;
    private final Map<String, String> headers;
    private final Http http;
    private final Hystrix hystrix;
    private final boolean isObservable;
    private final String cacheKeyGroup;
    private List<String> cookies = new ArrayList<>();
    private final RestAdapterConfig restAdapterConfig;
    

    public MethodInfo(final Method method, final RestAdapterConfig restAdapterConfig) {
        
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

        cacheKeyGroup = Arrays.stream(method.getAnnotations())
                        .filter(a -> CacheKeyGroup.class.equals(a.annotationType()))
                        .map(a -> a == null ? null : ((CacheKeyGroup)a).value())
                        .findFirst()
                        .orElse(null);

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
        
        @SuppressWarnings("rawtypes")
        final Class returnType = method.getReturnType();
        @SuppressWarnings("rawtypes")
        final Class httpClass = Class.class.equals(http.responseClass()) ? null : http.responseClass();
        
        this.isObservable = Observable.class.equals(returnType);
        if(this.isObservable){
            this.responseClass = Optional.ofNullable(httpClass).orElseThrow(() ->
                    new IllegalStateException("Http responseClass is required for observables"));
        }else{
            this.responseClass = Optional.ofNullable(httpClass).orElse(returnType);
        }
        
        this.restAdapterConfig = restAdapterConfig;

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
    

   
    
    private Map<String,String> getQueryParameters(Object[] args) {
        
        final Map<String, String> paramMap = new HashMap<>();
        
        for(int i = 0; i < parameters.length; i++){
            final Query queryParam = parameters[i].getAnnotation(Query.class);
            if(queryParam != null){
                final Object arg = args[i];
                
                if(arg != null){
                    Optional<?> value = Optional.empty();
                    if(Optional.class.equals(arg.getClass())){
                        final Optional<?> optional = ((Optional<?>)arg);
                        
                        if(optional.isPresent()){
                            value = optional;
                        }
                    }else if(com.google.common.base.Optional.class.isAssignableFrom(arg.getClass())){
                        final com.google.common.base.Optional<?> optional = ((com.google.common.base.Optional<?>)arg);
                        
                        if(optional.isPresent()){
                            value = Optional.ofNullable(optional.get());
                        }
                    }else{
                        value = Optional.ofNullable(arg);
                    }

                    value.ifPresent(v -> paramMap.put(queryParam.value(), String.valueOf(v)) );
                }
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
    
    public Setter getSetter() {
    
        return setter;
    }
    
    public Class<?> getResponseClass() {
    
        return responseClass;
    }

    public boolean isObservable() {
    
        return isObservable;
    }
    
    public String getCacheKey(Object[] args) {
        if (cacheKeyGroup != null) {
            return cacheKeyGroup + ":" + this.getRenderedPath(args);
        }

        return this.getRenderedPath(args);
    }

    public HttpRequest toHttpRequest(Object[] args) {

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
                .entity(restAdapterConfig.getMessageSerializer().writeValue(t));
            } catch (Exception e) {
                e.printStackTrace();  ///XXX: decide there is better exception handling
            }
        });
        
        HttpRequest request = requestBuilder.build();
        return request;
    } 
    
    
    
}