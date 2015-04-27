package com.kenzan.bowtie.serializer;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;

/***
 * <p>
 * {@link MessageSerializer} for serializing Jackson objects
 * </p>
 */
public class JacksonMessageSerializer implements MessageSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Object readValue(Class<?> clazz, InputStream inputStream) throws Exception {
        return objectMapper.reader(clazz).readValue(inputStream);
    }

    @Override
    public String writeValue(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

}
