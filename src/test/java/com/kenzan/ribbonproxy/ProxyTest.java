package com.kenzan.ribbonproxy;

import static org.junit.Assert.*;

import org.junit.Test;



public class ProxyTest {

    @Test
    public void test() {
        
        final RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint("http://localhost/service").build();
        final FakeClient fakeClient = restAdapter.create(FakeClient.class);
        
        fakeClient.getFakeResponse();
        
        fakeClient.getWithParameters("Owen");
    }

}
