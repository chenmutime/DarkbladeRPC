package com.darkblade.rpc.core.server;

import com.darkblade.rpc.common.util.SpiSupportUtil;
import com.darkblade.rpc.core.config.RpcCoreConfiguration;
import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.google.common.base.Strings;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class ServerDiscoveryBootstrap extends RpcCoreConfiguration implements InitializingBean, BeanFactoryAware {

    @Resource
    private ZookeeperServerProperties zookeeperProperties;

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
    }

    @Override
    public void afterPropertiesSet() {
        if (Strings.isNullOrEmpty(serviceCenter)) {
            serviceCenter = "zookeeper";
        }
        Map<String, ServerDiscovery> serverDiscoveryMap = new SpiSupportUtil().loadSPI(ServerDiscovery.class);
        ServerDiscovery serverDiscovery = serverDiscoveryMap.get(serviceCenter);
        serverDiscovery.startup(zookeeperProperties);
        beanFactory.registerSingleton(serviceCenter, serverDiscovery);
    }
}
