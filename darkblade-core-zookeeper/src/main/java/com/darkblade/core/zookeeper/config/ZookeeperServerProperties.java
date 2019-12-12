package com.darkblade.core.zookeeper.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "darkblade.rpc.zookeeper")
public class ZookeeperServerProperties {

    private String host = "localhost";

    private int port = 2181;

    private int sessionTimeout = 3000;


}
