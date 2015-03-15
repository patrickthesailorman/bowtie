package com.kenzan.ribbonproxy.model;

import com.fasterxml.jackson.annotation.JsonProperty;


public class FakeUserAddress {

    @JsonProperty
    private String address;
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    
}
