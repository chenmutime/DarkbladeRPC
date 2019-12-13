package com.darkblade.rpc.core.bootstrap;

import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.helper.ServiceMetadataManager;
import com.darkblade.rpc.core.netty.pool.ChannelPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 初始化调用连接池、服务中心
 */
public class DarkbladeClientBootstrap implements InitializingBean{

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterPropertiesSet() {
        logger.info("正在启动rpc客户端");
        startupNettyPool();
        startupServiceCenter();
    }

    /**
     * 初始化连接池
     */
    private void startupNettyPool() {
        ChannelPoolFactory.getInstance().initalizeChannelFactory();
    }

    @Autowired
    private ServerDiscovery serverDiscovery;

    /**
     * 启动注册中心
     */
    private void startupServiceCenter() {
        serverDiscovery.startup();
    }

}
