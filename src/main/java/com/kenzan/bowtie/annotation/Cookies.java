package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/***
 * <p>
 * Runtime method annotation to represent the static configuration of the
 * method. Used with or without parameter annotations.
 * </p>
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Cookies {

    /***
     * <p>
     * A list of static cookie values. Dynamic cookie values can be specified as
     * a parameter annotation
     * </p>
     * 
     * @return
     */
    public Cookie[] cookies() default {};

}
