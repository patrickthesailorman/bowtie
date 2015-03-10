package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.FormParam;
import com.kenzan.ribbonproxy.annotation.GET;
import com.kenzan.ribbonproxy.annotation.POST;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;


public interface FakeClient {

    @GET
    @Path("/service/endpoint")
    public FakeResponse getFakeResponse();
    
    
    @GET
    @Path("/service/endpoint/{name}")
    public FakeResponse getWithPathParameters(@PathParam("name") String name);
    
    
    @POST
    @Path("/service/endpoint/")
    public FakeResponse getWithFormParameters(@FormParam("name") String name);
    
}
