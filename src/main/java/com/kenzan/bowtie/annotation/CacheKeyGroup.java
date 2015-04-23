package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime method annotation to represent the CacheKeyGroup of the method.  The CacheKeyGroup
 * represents a grouping of CacheKeys in the CacheStore.  The CacheKey is composed of the CacheKey
 * and the request path.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface CacheKeyGroup {
    String value();
}
