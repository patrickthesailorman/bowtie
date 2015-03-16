package com.kenzan.ribbonproxy.serializer;

import java.io.InputStream;


public interface MessageSerializer {

    
    String writeValue(Object object) throws Exception;

    Object readValue(Class<?> clazz, InputStream inputStream) throws Exception;
}
