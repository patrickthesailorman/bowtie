package com.kenzan.bowtie.serializer;

import java.io.InputStream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
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
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.configure(
                MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
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
