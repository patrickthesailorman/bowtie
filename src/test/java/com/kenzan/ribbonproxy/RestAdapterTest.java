package com.kenzan.ribbonproxy;

import java.io.IOException;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kenzan.ribbonproxy.model.FakeUser;
import com.kenzan.ribbonproxy.model.FakeUserAddress;
import com.kenzan.ribbonproxy.model.FakeUsers;
import com.kenzan.ribbonproxy.serializer.JacksonMessageSerializer;
import com.netflix.client.http.HttpResponse;
import com.netflix.config.ConfigurationManager;



public class RestAdapterTest {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RestAdapterTest.class);
    
    private static FakeClient fakeClient;
    private static FakeUser user;
    
    @BeforeClass
    public static void beforeClass() throws IOException{
        
        LOGGER.info("Starting beforeClass");
        ConfigurationManager.loadPropertiesFromResources("sample-client.properties");
        
        final RestAdapter restAdapter = new RestAdapter
                        .Builder()
                        .setNamedClient("sample-client")
                        .setMessageSerializer(new JacksonMessageSerializer())
                        .build();
        fakeClient = restAdapter.create(FakeClient.class);
        
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
        FakeUsers users = fakeClient.getUsers("jdoe","020835c7-cf7e-4ba5-b117-4402e5d79079");
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(1));
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
}
