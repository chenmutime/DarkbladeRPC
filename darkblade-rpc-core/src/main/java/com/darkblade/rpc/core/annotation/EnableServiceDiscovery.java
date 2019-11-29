package com.darkblade.rpc.core.annotation;

import com.darkblade.rpc.core.config.AutoConfiguration;
import com.darkblade.rpc.core.registry.ClientRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({ClientRegistrar.class, AutoConfiguration.class})
public @interface EnableServiceDiscovery {

}
