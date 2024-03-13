package com.token.app.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller
public class TokenController {

    @Post(value = "/token", consumes = (MediaType.APPLICATION_FORM_URLENCODED))
    public HttpResponse<String> getToken(@Body String body) {
        String mockTokenResponse = "{\"access_token\":\"mock_access_token\",\"token_type\":\"bearer\",\"expires_in\":3600,\"refresh_token\":\"mock_refresh_token\",\"scope\":\"read write\"}";
        return HttpResponse.ok(mockTokenResponse).contentType(MediaType.APPLICATION_JSON_TYPE);
    }


}
