package com.darkblade.rpc.register.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rpc.server")
public class RpcProperties {

    private double limiter = 500;

    private int port = 10001;

    private String host = "localhost";

    private String serviceName = "rpc-server";

    private ZookeeperProperties zookeeper;

}
