package com.darkblade.rpc.register.config;

import com.darkblade.rpc.register.registry.ServerBootstrap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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



    @Bean
    public ServerBootstrap nrpcServerBootstrap(RpcProperties rpcProperties) {
        ServerBootstrap serverBootstrap = new ServerBootstrap(rpcProperties);
        return serverBootstrap;
    }


}
