package com.darkblade.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Data
@Configuration
@ConfigurationProperties(prefix = "com.darkblade.rpc.core")
public class ZookeeperServerProperties extends ServerProperties {

    private String host = "localhost";

    private int port = 2181;

    private int sessionTimeout = 3000;


}
