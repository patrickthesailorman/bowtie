package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseType {
    /***
     * <p>
     * Used mainly for observables to specify the return type because the return
     * type cannot be inferred due to type erasure. If specified for a
     * non-observable method, it will be honored over the returnType of the
     * method.
     * </p>
     * 
     * @return the response class for observables.
     */
    public Class<?> responseClass() default Class.class;
}
