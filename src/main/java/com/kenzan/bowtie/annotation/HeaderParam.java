package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime method or parameter annotation to represent the cookie of a request.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface HeaderParam {
    
    /***
     * Name of the header.
     *
     * @return
     */
    public String name();
    
    /***
     * Value of the header.  Default is "".
     *
     * @return
     */
    public String value() default "";
}
