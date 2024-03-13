package com.client.app.controller;

import com.client.app.client.ServerServiceClient;
import com.client.app.client.model.SampleRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutorService;

@Controller
@Secured(SecurityRule.IS_ANONYMOUS)
@Slf4j
public class ClientController {

    @Inject
    @Named(TaskExecutors.IO)
    private ExecutorService ioExecutor;

    @Inject
    private ServerServiceClient serverServiceClient;


    @Get(value = "/client-user/{username}", produces = {MediaType.APPLICATION_JSON})
    public Single<String> getPersonalizationsByOlbUsername(@PathVariable(value = "username") String userName) {
        return serverServiceClient.getUserId(new SampleRequest(userName))
                .subscribeOn(Schedulers.from(ioExecutor));
    }


}