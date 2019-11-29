package com.darkblade.rpc.core.annotation;

import com.darkblade.rpc.core.config.AutoConfiguration;
import com.darkblade.rpc.core.registry.NrpcClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({NrpcClientRegistrar.class, AutoConfiguration.class})
public @interface EnableServiceDiscovery {

}
