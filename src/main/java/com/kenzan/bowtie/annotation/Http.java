package com.kenzan.bowtie.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.netflix.client.http.HttpRequest.Verb;


/***
 * <p>
 * Runtime method annotation to represent the static configuration of the method.  Used with or without parameter annotations.
 * </p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Http {


    /***
     * <p>
     * Http {@link Verb} to use in the HTTP call.
     * </p>
     *
     * @return
     */
    public Verb method();
    
    
    /***
     * <p>
     * Specify the uri to use using curly braces for any variables.  For example "/user/{username}".  For any variables used
     * in the uriTemplate, corresponding @Path annotations must appear in the method signature.
     * </p>
     * 
     * <p>
     * The uriTemplate should always start with a "/"
     * </p>
     * 
     * @return
     */
    public String uriTemplate();


    /***
     * <p>
     * A list of static header values.  Dynamic header values can be specified as a parameter annotation
     * </p>
     * 
     * @return
     */
    public Header[] headers() default {};
    
    
    /***
     * <p>
     * A list of static cookie values.  Dynamic cookie values can be specified as a parameter annotation
     * </p>
     * 
     * @return
     */
    public Cookie[] cookies() default {};
    
    /***
     * <p>
     * Used mainly for observables to specify the return type because the return type cannot be inferred 
     * due to type erasure. If specified for a non-observable method, it will be honored over the returnType
     * of the method. 
     * <p>
     * 
     * @return
     */
    public Class<?> responseClass() default Class.class;
    
}
