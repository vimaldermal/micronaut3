package com.client.app.client;

import com.client.app.client.model.SampleRequest;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Retryable;
import io.reactivex.rxjava3.core.Single;

@Client(id = "server-service")
@Retryable(delay = "10ms", attempts = "2", maxDelay = "1s")
public interface ServerServiceClient {

    @Post("/server")
    Single<String> getUserId(@Body SampleRequest sampleRequest);


}
