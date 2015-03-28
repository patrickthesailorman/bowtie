package com.kenzan.bowtie;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kenzan.bowtie.annotation.Encoding;
import com.kenzan.bowtie.cache.GuavaRestCache;
import com.kenzan.bowtie.model.FakeUser;
import com.kenzan.bowtie.model.FakeUserAddress;
import com.kenzan.bowtie.model.FakeUsers;
import com.kenzan.bowtie.serializer.JacksonMessageSerializer;
import com.netflix.client.ClientException;
import com.netflix.client.http.HttpResponse;
import com.netflix.config.ConfigurationManager;



public class RestAdapterTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAdapterTest.class);
    
    private static FakeClient fakeClient;
    private static FakeClient fakeClient2;
    private static FakeClient fakeClient3;
    
    private static FakeUser user;
    
    private static GuavaRestCache cache;
    @BeforeClass
    public static void beforeClass() throws IOException{
        
        LOGGER.info("Starting beforeClass");
        ConfigurationManager.loadPropertiesFromResources("sample-client.properties");
        
        final RestAdapter restAdapter = RestAdapter.getNamedAdapter("sample-client");
        fakeClient = restAdapter.create(FakeClient.class);


        final RestAdapter restAdapter2 = RestAdapter.getNamedAdapter("sample-client", RestAdapterConfig.custom()
                        .withMessageSerializer(new JacksonMessageSerializer())
                        .withEncoding(Encoding.gzip)
                        .build());

        fakeClient2 = restAdapter2.create(FakeClient.class);

        cache = new GuavaRestCache();

        final RestAdapter restAdapter3 = RestAdapter.getNamedAdapter("sample-client", RestAdapterConfig.custom()
                .withMessageSerializer(new JacksonMessageSerializer())
                .withRestCache(cache)
                .build());

        fakeClient3 = restAdapter3.create(FakeClient.class);

        user = new FakeUser();
        user.setName("John Doe");
    }
    

    @Test
    public void testGetUser() {
        LOGGER.info("Starting testGetUser");
        FakeUser user = fakeClient.getUser("jdoe");
        Assert.assertThat(user.getName(), IsEqual.equalTo("John Doe"));
    }
    
    


    @Test
    public void testGetUserObservable() {
        LOGGER.info("Starting testGetUserObservable");
        FakeUser user = fakeClient.getUserObservable("jdoe").toBlockingObservable().single();
        Assert.assertThat(user.getName(), IsEqual.equalTo("John Doe"));
    }
    
    
    @Test
    public void testGetUserAddress() {
        LOGGER.info("Starting testGetUserAddress");
        FakeUserAddress address = fakeClient.getUserAddress("jdoe","address");
        Assert.assertThat(address.getAddress(), IsEqual.equalTo("1060 W Addison St, Chicago, IL 60613"));
    }
    
    
    @Test
    public void testGetUsers() {
        LOGGER.info("Starting testGetUsers");
        FakeUsers users = fakeClient2.getUsers("bbelcher", Optional.ofNullable("email"),
            "020835c7-cf7e-4ba5-b117-4402e5d79079");
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(2));
    }
    
    
    @Test
    public void testGetUsersOptional() {
        LOGGER.info("Starting testGetUsersOptional");
        FakeUsers users = fakeClient.getUsers("jdoe", Optional.empty(),
            "020835c7-cf7e-4ba5-b117-4402e5d79079");
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(1));
    }
    
    @Test
    public void testGetRoleUsers() {
        LOGGER.info("Starting testGetRoleUsers");
        FakeUsers users = fakeClient.getRoleUsers(com.google.common.base.Optional.fromNullable("vanessa"));
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(0));
    }
    
    
    @Test
    public void testEmailUser() {
        LOGGER.info("Starting testEmailUser");
        HttpResponse response = fakeClient.emailUser(user);
        Assert.assertThat(response.getStatus(), IsEqual.equalTo(200));
    }

    
    @Test
    public void testDeleteUser() {
        LOGGER.info("Starting testDeleteUser");
        HttpResponse response = fakeClient.deleteUser("jdoe");
        Assert.assertThat(response.getStatus(), IsEqual.equalTo(200));
    }
    
    @Test
    public void testMutateUser() {
        LOGGER.info("Starting testMutateUser");
        HttpResponse response = fakeClient.mutateUser(user,"aa8a2e85-412e-46a2-889f-b2c133a59c89");
        Assert.assertThat(response.getStatus(), IsEqual.equalTo(200));
    }

    @Test
    public void testGetCachedUser() throws JsonParseException, JsonMappingException, IOException, ClientException {
        LOGGER.info("Starting testGetCachedUser");
        FakeUser user = fakeClient3.getCachedUser("bdoe");
        Assert.assertThat(user.getName(), IsEqual.equalTo("Bob Doe"));
        Assert.assertThat(
            cache.get("userCache:/user/bdoe")
            .map(t -> {
                try {
                    return new ObjectMapper().readValue(new ByteArrayInputStream(t.getCachedBytes()),
                        FakeUser.class).getName();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).get(),
            IsEqual.equalTo("Bob Doe"));
        
        
        user = fakeClient3.getCachedUser("bdoe");
        Assert.assertThat(user.getName(), IsEqual.equalTo("Bob Doe"));
        
        
        HttpResponse response = fakeClient3.getCachedUserResponse("bdoe");
        Assert.assertThat(response.getStatus(), IsEqual.equalTo(200));
        
        user = new ObjectMapper().readValue(response.getInputStream(), FakeUser.class);
        Assert.assertThat(user.getName(), IsEqual.equalTo("Bob Doe"));
    }
}
