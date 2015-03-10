package com.kenzan.ribbonproxy;

import static org.junit.Assert.*;

import org.junit.Test;



public class ProxyTest {

    @Test
    public void test() {
        
        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://localhost/service").build();
        final FakeClient fakeClient = restAdapter.create(FakeClient.class);
        System.out.println("What's my class?  " + fakeClient.getClass().getName());
        
        fakeClient.getFakeResponse();
        
        fakeClient.getWithPathParameters("Owen");
        
        fakeClient.getWithFormParameters("Nick");
        
        //cache?
        fakeClient.getFakeResponse();
    }

}
