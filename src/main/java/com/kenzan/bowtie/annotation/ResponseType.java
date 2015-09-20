package com.kenzan.bowtie.annotation;

public @interface ResponseType {
    /***
     * <p>
     * Used mainly for observables to specify the return type because the return
     * type cannot be inferred due to type erasure. If specified for a
     * non-observable method, it will be honored over the returnType of the
     * method.
     * <p>
     * 
     * @return
     */
    public Class<?> responseClass() default Class.class;
}
