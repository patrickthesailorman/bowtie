package com.kenzan.ribbonproxy;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.matchers.Times;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;

public class MockServerInitializationClass implements ExpectationInitializer {

    @Override
    public void initializeExpectations(MockServerClient mockServerClient) {
        
        getUser(mockServerClient);
        
        getUserAddress(mockServerClient);
        
        getUsers(mockServerClient);
        
        emailUser(mockServerClient);
        
        deleteUser(mockServerClient);
        
        mutateUser(mockServerClient);
        
    }

    private void emailUser(MockServerClient mockServerClient) {

        mockServerClient.
        dumpToLog().
        when(
            HttpRequest.request()
            .withMethod("POST")
            .withPath("/user/email")
            .withBody("{\"name\":\"John Doe\"}"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            
        );        
    }

    private void getUsers(MockServerClient mockServerClient) {

        mockServerClient
        .dumpToLog()
        .when(
            HttpRequest.request()
            .withMethod("GET")
            .withPath("/user")
            .withHeader(Header.header("X-SESSION-ID", "020835c7-cf7e-4ba5-b117-4402e5d79079"))
            .withQueryStringParameter(Parameter.param("byUsername", "jdoe")),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            .withBody("{ \"users\" : [{ \"name\" : \"John Doe\" }] }")
        );
    }

    private void getUser(MockServerClient mockServerClient) {

        mockServerClient
        .dumpToLog()
        .when(
            HttpRequest.request()
            .withMethod("GET")
            .withHeader(Header.header("X-SESSION-ID", "55892d6d-77df-4617-b728-6f5de97f5752"))
            .withPath("/user/jdoe"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            .withBody("{ \"name\" : \"John Doe\" }")
        );
    }
    
    
    private void getUserAddress(MockServerClient mockServerClient) {

        mockServerClient
        .dumpToLog()
        .when(
            HttpRequest.request()
            .withMethod("GET")
            .withPath("/user/address/jdoe"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            .withBody("{ \"address\" : \"1060 W Addison St, Chicago, IL 60613\" }")
        );
    }
    
    private void deleteUser(MockServerClient mockServerClient) {

        mockServerClient
        .dumpToLog()
        .when(
            HttpRequest.request()
            .withMethod("DELETE")
            .withPath("/user/jdoe"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
        );
    }

    private void mutateUser(MockServerClient mockServerClient) {
    
        mockServerClient.
        dumpToLog().
        when(
            HttpRequest.request()
            .withMethod("PUT")
            .withPath("/user")
            .withBody("{\"name\":\"John Doe\"}"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            
        );        
    }   
    
    
}