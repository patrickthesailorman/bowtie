package com.kenzan.ribbonproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;



public class FakeUser {

    @JsonProperty
    private String name;
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
}
