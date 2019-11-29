package com.darkblade.rpc.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Component
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NrpcClient {

    String serviceName();

    long timeout() default 3000L;

    TimeUnit timeUnit() default TimeUnit.MILLISECONDS;

    int retries() default 2;
}
