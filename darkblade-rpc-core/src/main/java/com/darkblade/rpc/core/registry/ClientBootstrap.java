package com.darkblade.rpc.core.registry;

import com.darkblade.rpc.core.config.ZookeeperProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientBootstrap {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZookeeperProperties zookeeperProperties;

    public ClientBootstrap(ZookeeperProperties zookeeperProperties) {
        this.zookeeperProperties = zookeeperProperties;
        logger.info("正在启动rpc客户端");
        start();
    }

    /**
     * 初始化连接池
     */
    private void start() {
        ServiceManager.getInstance().initalizeChannelFactory(zookeeperProperties);
    }

}
