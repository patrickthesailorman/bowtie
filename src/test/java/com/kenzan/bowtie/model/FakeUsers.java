package com.kenzan.bowtie.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;


public class FakeUsers {
    
    @JsonProperty
    private List<FakeUser> users = new ArrayList<>();
    
    
    public void setUsers(List<FakeUser> users) {
        this.users = users;
    }
    
    
    public List<FakeUser> getUsers() {
        return users;
    }

}
