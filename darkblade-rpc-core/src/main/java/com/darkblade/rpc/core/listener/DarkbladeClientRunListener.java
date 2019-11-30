package com.darkblade.rpc.core.listener;

import com.darkblade.rpc.core.config.ZookeeperProperties;
import com.darkblade.rpc.core.server.ServiceManager;
import com.darkblade.rpc.core.zookeeper.ZkServerDiscovery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplicationRunListener;
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
        ServiceManager.getInstance().initalizeChannelFactory();
    }

    @Autowired
    private ZookeeperProperties zookeeperProperties;

    /**
     * 启动注册中心
     */
    private void startupRegistrationCenter() {
        new ZkServerDiscovery(zookeeperProperties);
    }
}
