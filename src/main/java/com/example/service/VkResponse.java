package com.example.service;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Description:
 * Creation date: 11.07.2016 18:03
 *
 * @author sks
 */
public class VkResponse<S> {

    private S[] array;

    public S[] getArray() {
        return array;
    }
    @JsonProperty("response")
    public void setArray(S[] array) {
        this.array = array;
    }
}
