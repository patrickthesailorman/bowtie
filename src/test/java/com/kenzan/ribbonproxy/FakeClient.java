package com.kenzan.ribbonproxy;

import rx.Observable;

import com.kenzan.ribbonproxy.annotation.Body;
import com.kenzan.ribbonproxy.annotation.Header;
import com.kenzan.ribbonproxy.annotation.Http;
import com.kenzan.ribbonproxy.annotation.Hystrix;
import com.kenzan.ribbonproxy.annotation.Path;
import com.kenzan.ribbonproxy.annotation.Query;
import com.kenzan.ribbonproxy.model.FakeUser;
import com.kenzan.ribbonproxy.model.FakeUserAddress;
import com.kenzan.ribbonproxy.model.FakeUsers;
import com.netflix.client.http.HttpRequest.Verb;
import com.netflix.client.http.HttpResponse;


public interface FakeClient {
    public static final String GROUP_KEY = "FakeGroup";
    public static final String COMMAND_KEY = "FakeCommand";
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user/{username}",
        headers = {
            @Header(name="X-SESSION-ID", value="55892d6d-77df-4617-b728-6f5de97f5752")
        }
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public FakeUser getUser(@Path("username") String name);
    
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user/{username}",
        headers = {
            @Header(name="X-SESSION-ID", value="55892d6d-77df-4617-b728-6f5de97f5752")
        },
        responseClass=FakeUser.class
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public Observable<FakeUser> getUserObservable(@Path("username") String name);
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user/{field}/{username}"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public FakeUserAddress getUserAddress(@Path("username") String name,
                                   @Path("field") String field);
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public FakeUsers getUsers(@Query("byUsername") String username, 
                              @Header(name="X-SESSION-ID") String sessionId);
    
    
    @Http(
        method = Verb.PUT,
        uriTemplate = "/user"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public HttpResponse mutateUser(@Body FakeUser user);
    
    

    @Http(
        method = Verb.POST,
        uriTemplate = "/user/email"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public HttpResponse emailUser(@Body FakeUser user);
    
    
    
    
    
    @Http(
        method = Verb.DELETE,
        uriTemplate = "/user/{username}"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public HttpResponse deleteUser(@Path("username") String name);
    
}
