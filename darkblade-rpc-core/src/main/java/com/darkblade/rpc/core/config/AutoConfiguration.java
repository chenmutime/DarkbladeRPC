package com.darkblade.rpc.core.config;

import com.darkblade.rpc.core.zookeeper.ZkServerDiscovery;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.darkblade.rpc.core"})
public class AutoConfiguration {


}
