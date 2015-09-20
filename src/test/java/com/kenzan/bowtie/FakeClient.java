package com.kenzan.bowtie;

import java.util.Optional;

import rx.Observable;

import com.kenzan.bowtie.annotation.Body;
import com.kenzan.bowtie.annotation.CacheKeyGroup;
import com.kenzan.bowtie.annotation.Cookie;
import com.kenzan.bowtie.annotation.Cookies;
import com.kenzan.bowtie.annotation.HeaderParam;
import com.kenzan.bowtie.annotation.HystrixGroup;
import com.kenzan.bowtie.annotation.Path;
import com.kenzan.bowtie.annotation.Query;
import com.kenzan.bowtie.annotation.ResponseType;
import com.kenzan.bowtie.model.FakeUser;
import com.kenzan.bowtie.model.FakeUserAddress;
import com.kenzan.bowtie.model.FakeUsers;
import com.netflix.client.http.HttpResponse;
import com.netflix.ribbon.proxy.annotation.Http;
import com.netflix.ribbon.proxy.annotation.Http.HttpMethod;

/***
 * <p>
 * Example client used for unit testing as well as to demonstrate a variety of
 * use cases
 * </p>
 * 
 */
public interface FakeClient {
    public static final String GROUP_KEY = "FakeGroup";
    public static final String COMMAND_KEY = "FakeCommand";

    @Http(method = HttpMethod.GET, uri = "/user/{username}", headers = { @Http.Header(name = "X-SESSION-ID", value = "55892d6d-77df-4617-b728-6f5de97f5752") })
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public FakeUser getUser(
            @com.kenzan.bowtie.annotation.Path("username") String name);

    @Http(method = HttpMethod.GET, uri = "/user/{username}", headers = { @Http.Header(name = "X-SESSION-ID", value = "55892d6d-77df-4617-b728-6f5de97f5752") })
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    @CacheKeyGroup("userCache")
    public FakeUser getCachedUser(@Path("username") String name);

    @Http(method = HttpMethod.GET, uri = "/user/{username}", headers = { @Http.Header(name = "X-SESSION-ID", value = "55892d6d-77df-4617-b728-6f5de97f5752") })
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    @CacheKeyGroup("userCache")
    public HttpResponse getCachedUserResponse(@Path("username") String name);

    @Http(method = HttpMethod.GET, uri = "/user/{username}", headers = { @Http.Header(name = "X-SESSION-ID", value = "55892d6d-77df-4617-b728-6f5de97f5752") })
    @ResponseType(responseClass = FakeUser.class)
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public Observable<FakeUser> getUserObservable(@Path("username") String name);

    @Http(method = HttpMethod.GET, uri = "/user/{field}/{username}", headers = { @Http.Header(name = "Cache-Control", value = "no-cache") })
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public FakeUserAddress getUserAddress(@Path("username") String name,
            @Path("field") String field);

    @Http(method = HttpMethod.GET, uri = "/user")
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public FakeUsers getUsers(@Query("byUsername") String username,
            @Query("bySystem") Optional<String> system,
            @HeaderParam(name = "X-SESSION-ID") String sessionId);

    @Http(method = HttpMethod.GET, uri = "/user/role")
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public FakeUsers getRoleUsers(
            @Query("byRole") com.google.common.base.Optional<String> role);

    @Http(method = HttpMethod.PUT, uri = "/user")
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public HttpResponse mutateUser(@Body FakeUser user,
            @Cookie(name = "session") String sessionId);

    @Http(method = HttpMethod.POST, uri = "/user/email", headers = { @Http.Header(name = "Cookie", value = "username=jdoe") })
    @Cookies(cookies = { @Cookie(name = "session", value = "0a1bc2a7-11ef-4781-9c06-8d9b42719797") })
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public HttpResponse emailUser(@Body FakeUser user);

    @Http(method = HttpMethod.DELETE, uri = "/user/{username}")
    @HystrixGroup(groupKey = GROUP_KEY, commandKey = COMMAND_KEY)
    public HttpResponse deleteUser(@Path("username") String name);

}
