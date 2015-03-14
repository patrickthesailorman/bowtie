package com.kenzan.ribbonproxy;

import java.io.IOException;

import org.hamcrest.core.IsEqual;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.kenzan.ribbonproxy.model.FakeUserResponse;
import com.kenzan.ribbonproxy.model.FakeUsersResponse;
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
    public void testGetPathParams() {
        FakeUserResponse user = fakeClient.getUser("jdoe");
        Assert.assertThat(user.getName(), IsEqual.equalTo("John Doe"));
    }
    
    @Test
    public void testGetFormParams() {
        FakeUsersResponse users = fakeClient.getUsers("jdoe");
        Assert.assertThat(users.getUsers().size(), IsEqual.equalTo(1));
    }

}
