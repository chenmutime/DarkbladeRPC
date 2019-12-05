package com.darkblade.rpc.register.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.darkblade.rpc.register"})
public class RegisterAutoConfiguration {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public RegisterAutoConfiguration() {
        logger.info("starting netty server...");
    }

}
