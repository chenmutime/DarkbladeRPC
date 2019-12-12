package com.darkblade.core.zookeeper.config;

import com.darkblade.core.zookeeper.discovery.ZookeeperServerDiscovery;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ZookeeperServerProperties.class)
public class ZookeeperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerDiscovery serverDiscovery(ZookeeperServerProperties zookeeperServerProperties) {
        ServerDiscovery serverDiscovery = new ZookeeperServerDiscovery(zookeeperServerProperties);
        return serverDiscovery;
    }
}
