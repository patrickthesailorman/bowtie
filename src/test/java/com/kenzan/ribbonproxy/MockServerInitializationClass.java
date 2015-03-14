package com.kenzan.ribbonproxy;

import org.mockserver.client.server.MockServerClient;
import org.mockserver.initialize.ExpectationInitializer;
import org.mockserver.matchers.Times;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;

public class MockServerInitializationClass implements ExpectationInitializer {

    public static MockServerClient mockServerClient;

    @Override
    public void initializeExpectations(MockServerClient mockServerClient) {
        MockServerInitializationClass.mockServerClient = mockServerClient;
        
        mockServerClient.when(
            HttpRequest.request()
            .withMethod("GET")
            .withPath("/user/jdoe"),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            .withBody("{ \"name\" : \"John Doe\" }")
        );
        
        mockServerClient.when(
            HttpRequest.request()
            .withMethod("GET")
            .withPath("/user")
            .withQueryStringParameter(Parameter.param("byUsername", "jdoe")),
            Times.unlimited()
        ).respond(
            HttpResponse.response()
            .withStatusCode(200)
            .withBody("{ \"users\" : [{ \"name\" : \"John Doe\" }] }")
        );
        
        mockServerClient.dumpToLog();
        
    }
}