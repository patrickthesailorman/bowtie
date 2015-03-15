package com.kenzan.ribbonproxy;

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.Http;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.kenzan.ribbonproxy.model.FakeUser;
import com.kenzan.ribbonproxy.model.FakeUsers;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;


public interface FakeClient {

    @Http(
        method = Verb.GET,
        uriTemplate = "/user/{name}"
    )
    @Header(name="X-SESSION-ID", value="55892d6d-77df-4617-b728-6f5de97f5752")
    public FakeUser getUser(@Path("username") String name);
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user"
    )
    public FakeUsers getUsers(@Query("byUsername") String username, 
                              @Header(name="X-SESSION-ID") String sessionId);
    
    
    @Http(
        method = Verb.POST,
        uriTemplate = "/user/email"
    )
    public HttpResponse emailUser(@Body FakeUser user);
    
}
