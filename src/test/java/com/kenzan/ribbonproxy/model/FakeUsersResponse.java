package com.kenzan.ribbonproxy.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class FakeUsersResponse {
    
    @JsonProperty
    private List<FakeUserResponse> users = new ArrayList<>();
    
    
    public void setUsers(List<FakeUserResponse> users) {
        this.users = users;
    }
    
    
    public List<FakeUserResponse> getUsers() {
        return users;
    }

}
