package com.kenzan.bowtie;

import java.util.Optional;

import rx.Observable;

import com.kenzan.bowtie.annotation.Body;
import com.kenzan.bowtie.annotation.CacheKeyGroup;
import com.kenzan.bowtie.annotation.Cookie;
import com.kenzan.bowtie.annotation.Header;
import com.kenzan.bowtie.annotation.Http;
import com.kenzan.bowtie.annotation.Hystrix;
import com.kenzan.bowtie.annotation.Path;
import com.kenzan.bowtie.annotation.Query;
import com.kenzan.bowtie.model.FakeUser;
import com.kenzan.bowtie.model.FakeUserAddress;
import com.kenzan.bowtie.model.FakeUsers;
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
        }
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    @CacheKeyGroup("userCache")
    public FakeUser getCachedUser(@Path("username") String name);
    
    
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
    @CacheKeyGroup("userCache")
    public HttpResponse getCachedUserResponse(@Path("username") String name);
    

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
                              @Query("bySystem") Optional<String> system,
                              @Header(name="X-SESSION-ID") String sessionId);
    
    
    
    
    @Http(
        method = Verb.GET,
        uriTemplate = "/user/role"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public FakeUsers getRoleUsers(@Query("byRole") com.google.common.base.Optional<String> role);
    
                              
                              
    @Http(
        method = Verb.PUT,
        uriTemplate = "/user"
    )
    @Hystrix(
        groupKey=GROUP_KEY, commandKey=COMMAND_KEY
        )
    public HttpResponse mutateUser(@Body FakeUser user, @Cookie(name="session") String sessionId);
    
    

    @Http(
        method = Verb.POST,
        uriTemplate = "/user/email",
        headers={
          @Header(name="Cookie",value="username=jdoe")  
        },
        cookies={
            @Cookie(name="session", value="0a1bc2a7-11ef-4781-9c06-8d9b42719797")
        }
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
