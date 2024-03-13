package com.server.app.controller;


import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.annotation.Header;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Introspected
@AllArgsConstructor
@Data
public class ApiRequestBean {


    @Header(value = "Authorization")
    @NotBlank
    private String authorizeToken;


    @Header(value = "X-API-VERSION")
    @Nullable
    private String apiVersion;


    @Header(value = "X-CORRELATION-ID")
    @Nullable
    private String correlationId;
}
