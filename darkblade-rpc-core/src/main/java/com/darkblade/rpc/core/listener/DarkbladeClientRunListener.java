package com.darkblade.rpc.core.listener;

import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import com.darkblade.rpc.core.discovery.zookeeper.ZkServerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * 初始化服务
 */
public class DarkbladeClientRunListener implements ApplicationListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        logger.info("正在启动rpc客户端");
        startupConnectionPool();
        startupRegistrationCenter();
    }

    /**
     * 初始化连接池
     */
    private void startupConnectionPool() {
        ServiceMetadataManager.getInstance().initalizeChannelFactory();
    }

    @Autowired
    private ZookeeperServerProperties zookeeperProperties;

    /**
     * 启动注册中心
     */
    private void startupRegistrationCenter() {
        ServerDiscovery serverDiscovery = new ZkServerDiscovery();
        serverDiscovery.loadAllServices(zookeeperProperties);
    }
}
