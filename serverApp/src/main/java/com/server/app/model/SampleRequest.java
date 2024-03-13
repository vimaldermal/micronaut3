package com.server.app.model;

import io.micronaut.core.annotation.Introspected;
import lombok.Data;

@Data
@Introspected
public class SampleRequest {

    private String userName;
}
