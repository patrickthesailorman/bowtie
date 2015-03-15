package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.GET;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.POST;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.kenzan.ribbonproxy.model.FakeUser;
import com.kenzan.ribbonproxy.model.FakeUsers;
import com.netflix.client.http.HttpResponse;


public interface FakeClient {

    @GET("/user/{name}")
    @Header(name="X-SESSION-ID", value="55892d6d-77df-4617-b728-6f5de97f5752")
    public FakeUser getUser(@Path("username") String name);
    
    
    @GET("/user")
    public FakeUsers getUsers(@Query("byUsername") String username, 
                              @Header(name="X-SESSION-ID") String sessionId);
    
    
    @POST("/user/email")
    public HttpResponse emailUser(@Body FakeUser user);
    
}
