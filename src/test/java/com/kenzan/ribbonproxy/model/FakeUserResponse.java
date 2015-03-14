package com.kenzan.ribbonproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;



public class FakeUserResponse {

    @JsonProperty
    private String name;
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
}
