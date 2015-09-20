package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * <p>
 * Runtime parameter annotation to represent a path parameter of a request.  The value represents the name of the path parameter in the uriTemplate.
 * </p>
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Path {

    String value();

}
