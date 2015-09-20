package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/***
 * <p>
 * Runtime method annotation used to specify the Hystrix settings 
 * </p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HystrixGroup {
    
    /***
     * <p>
     * Hystrix groupKey value
     * </p>
     * 
     * @return
     */
    public String groupKey();
    
    /***
     * <p>
     * Hystrix commandKey value
     * </p>
     * 
     * @return
     */
    public String commandKey();
    
}
