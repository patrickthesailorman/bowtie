package com.kenzan.ribbonproxy;

import java.io.IOException;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.kenzan.ribbonproxy.model.FakeUser;
import com.kenzan.ribbonproxy.model.FakeUserAddress;
import com.kenzan.ribbonproxy.model.FakeUsers;
import com.netflix.client.http.HttpResponse;
import com.netflix.config.ConfigurationManager;



public class RestAdapterTest {
    
    private static FakeClient fakeClient;
    
    @BeforeClass
    public static void beforeClass() throws IOException{
        ConfigurationManager.loadPropertiesFromResources("sample-client.properties");
        
        final RestAdapter restAdapter = new RestAdapter
                        .Builder()
                        .setNamedClient("sample-client")
                        .build();
        fakeClient = restAdapter.create(FakeClient.class);
    }

    @Test
    public void testGetUser() {
        FakeUser user = fakeClient.getUser("jdoe");
        Assert.assertThat(user.getName(), IsEqual.equalTo("John Doe"));
    }
    
    
    @Test
    public void testGetUserAddress() {
        FakeUserAddress address = fakeClient.getUserAddress("jdoe","address");
        Assert.assertThat(address.getAddress(), IsEqual.equalTo("1060 W Addison St, Chicago, IL 60613"));
    }
    
    
    @Test
    public void testGetUsers() {
        FakeUsers users = fakeClient.getUsers("jdoe","020835c7-cf7e-4ba5-b117-4402e5d79079");
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(1));
    }
    
    @Test
    public void emailUser() {
        FakeUser user = new FakeUser();
        user.setName("John Doe");
        
        HttpResponse response = fakeClient.emailUser(user);
        Assert.assertThat(response.getStatus(), IsEqual.equalTo(200));
    }

}
