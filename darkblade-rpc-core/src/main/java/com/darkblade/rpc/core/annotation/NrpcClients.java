package com.darkblade.rpc.core.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
public @interface NrpcClients {

    String basePackage();

}
