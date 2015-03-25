package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.netflix.client.http.HttpRequest.Verb;


@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Http {
    
    public Verb method();
    
    public String uriTemplate();
    
    public Header[] headers() default {};
    
    public Cookie[] cookies() default {};
    
    public Class<?> responseClass() default Class.class;
    
}
