package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime parameter annotation to represent the cookie of a request.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
public @interface Cookie {
    
    /***
     * Name of the cookie
     * @return
     */
    public String name();
    
    /***
     * Value of the cookie.  The default is "".
     * @return
     */
    public String value() default "";
}
