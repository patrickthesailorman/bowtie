package com.kenzan.bowtie.serializer;

import java.io.InputStream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;

/***
 * <p>
 * {@link MessageSerializer} for serializing Jackson objects
 * </p>
 */
public class JacksonMessageSerializer implements MessageSerializer {

    private final ObjectMapper objectMapper;

    public JacksonMessageSerializer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public JacksonMessageSerializer() {
        this(new ObjectMapper());
        objectMapper.registerModule(new JaxbAnnotationModule());
    }

    @Override
    public Object readValue(Class<?> clazz, InputStream inputStream)
            throws Exception {
        return objectMapper.reader(clazz).readValue(inputStream);
    }

    @Override
    public String writeValue(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

}
