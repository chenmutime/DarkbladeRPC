package com.darkblade.rpc.core.server;

import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.discovery.ZkServerDiscovery;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ClientBootstrap implements ApplicationContextAware, InitializingBean {

    @Resource
    private ZookeeperServerProperties zookeeperProperties;

    @Override
    public void afterPropertiesSet() throws Exception {

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ServerDiscovery serverDiscovery = new ZkServerDiscovery();
        serverDiscovery.loadAllServices(zookeeperProperties);
    }
}
