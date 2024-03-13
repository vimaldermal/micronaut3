package com.client.app.client.model;

import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@Introspected
@AllArgsConstructor
public class SampleRequest {

    //TODO:actual userName:Purposefully renamed to userID to retry 
    private String userID;
}
