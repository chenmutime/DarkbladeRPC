package com.darkblade.rpc.core.config;

import com.darkblade.rpc.core.registry.NrpcClientBootstrap;
import com.darkblade.rpc.core.zookeeper.ZkServerDiscovery;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.darkblade.rpc.core"})
public class AutoConfiguration {

    /**
     * 服务发现必须先于nrpcClientBootstrap启动，因此放在前面
     * @param zookeeperProperties
     * @return
     */
    @Bean
    public ZkServerDiscovery nrpcServerDiscovery(ZookeeperProperties zookeeperProperties){
        ZkServerDiscovery zkServerDiscovery = new ZkServerDiscovery(zookeeperProperties);
        return zkServerDiscovery;
    }

    @Bean
    public NrpcClientBootstrap nrpcClientBootstrap(ZookeeperProperties zookeeperProperties){
        NrpcClientBootstrap nrpcClientBootstrap = new NrpcClientBootstrap(zookeeperProperties);
        return nrpcClientBootstrap;
    }

}
