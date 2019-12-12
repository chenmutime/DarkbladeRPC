package com.darkblade.rpc.register.annotation;

import com.darkblade.rpc.register.bootstrap.DarkBladeServerBootstrap;
import com.darkblade.rpc.register.config.NettyProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Configuration
@Import({NettyProperties.class, DarkBladeServerBootstrap.class})
public @interface EnableServerRegister {
}
