package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.GET;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;


public interface FakeClient {

    @GET
    @Path("/service/endpoint")
    public FakeResponse getFakeResponse();
    
    
    @GET
    @Path("/service/endpoint/{name}")
    public FakeResponse getWithParameters(@PathParam("name") String name);
    
}
