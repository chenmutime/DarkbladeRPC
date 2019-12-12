package com.darkblade.zookeeper.config;

import com.darkblade.rpc.register.registry.ServerRegister;
import com.darkblade.zookeeper.register.ZookeeperServerRegister;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ZookeeperServerProperties.class)
public class ZookeeperAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ServerRegister serverRegister(ZookeeperServerProperties zookeeperServerProperties){
        ServerRegister serverRegister = new ZookeeperServerRegister(zookeeperServerProperties);
        return serverRegister;
    }

}
