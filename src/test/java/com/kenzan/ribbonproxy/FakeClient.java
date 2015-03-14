package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.QueryParam;
import com.kenzan.ribbonproxy.annotation.GET;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.PathParam;
import com.kenzan.ribbonproxy.model.FakeUserResponse;
import com.kenzan.ribbonproxy.model.FakeUsersResponse;


public interface FakeClient {

    @GET
    @Path("/user/{name}")
    public FakeUserResponse getUser(@PathParam("username") String name);
    
    @GET
    @Path("/user")
    public FakeUsersResponse getUsers(@QueryParam("byUsername") String username);
    
}
