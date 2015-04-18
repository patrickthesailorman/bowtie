package com.kenzan.bowtie;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.ReaderWriter;

/***
 * Adds debug logging of the request and response to the logs using slf4j.
 */
public class LoggerFilter extends ClientFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerFilter.class);
    
    @Override
    public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

        String uuid = UUID.randomUUID().toString();
        
        if(LOGGER.isDebugEnabled()){
            logRequest(uuid, request);
        }

        ClientResponse response = getNext().handle(request);
        
        if(LOGGER.isDebugEnabled()){
            logResponse(uuid, response);
        }
        
        return response;
    }

    private void logResponse(String uuid, ClientResponse response) {
        

        final StringBuffer sb = new StringBuffer();
        Status status = response.getClientResponseStatus();
        sb.append(uuid + "\n" + status.getStatusCode() + " " + status.getReasonPhrase() + "\n");
        
        
        response.getHeaders().forEach((k, v) -> {
            String value = v.stream()
                            .map(e -> (e == null ? "" : e.toString()))
                            .collect(Collectors.joining(";"));
            sb.append(k + ": " + value + "\n");
        });
        
        try(ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream entityInputStream = response.getEntityInputStream(); ){
            ReaderWriter.writeTo(entityInputStream, out);

            byte[] requestEntity = out.toByteArray();
            String body = new String(requestEntity, Charsets.UTF_8);
            if(body.length() > 0){
                sb.append("Body: " + body);
            }
            
            response.setEntityInputStream(new ByteArrayInputStream(requestEntity));
        } catch (IOException ex) {
            throw new ClientHandlerException(ex);
        }
        
        LOGGER.debug(sb.toString());
         
    }

    private void logRequest(String uuid, ClientRequest request) {

        
        final StringBuffer sb = new StringBuffer();
        
        sb.append(uuid + "\n" + request.getMethod() + " " + request.getURI() + "\n");
        
        request.getHeaders().forEach((k, v) -> {
            String value = v.stream()
                            .map(e -> (e == null ? "" : e.toString()))
                            .collect(Collectors.joining(";"));
            sb.append(k + ": " + value + "\n");
        });
        
        Optional.ofNullable(request.getEntity())
        .ifPresent(e -> sb.append("Body: " + e));
        
        
        LOGGER.debug(sb.toString());
    }

}
