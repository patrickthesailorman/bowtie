package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime parameter annotation to represent a query parameter of a request.   The value represents the name of the query parameter.
 * </p>
 */
@Target({ElementType.TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    public String value();
}
