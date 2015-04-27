package com.kenzan.bowtie.serializer;

import java.io.InputStream;

/***
 * <p> 
 * Interface for serializing messages
 * </p>
 */
public interface MessageSerializer {

    
    String writeValue(Object object) throws Exception;

    Object readValue(Class<?> clazz, InputStream inputStream) throws Exception;
}
