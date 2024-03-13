package com.server.app.controller;

import com.server.app.model.SampleRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.RequestBean;
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
@Secured(SecurityRule.IS_AUTHENTICATED)
@Slf4j
public class ServerController {

    @Inject
    @Named(TaskExecutors.IO)
    private ExecutorService ioExecutor;


    @Post(value = "/server", produces = {MediaType.APPLICATION_JSON})
    public Single<String> getUserName(@RequestBean ApiRequestBean apiRequestBean, @Body SampleRequest sampleRequest) {
        return Single.just(sampleRequest.getUserName())
                .subscribeOn(Schedulers.from(ioExecutor));
    }


}