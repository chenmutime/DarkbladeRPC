package com.darkblade.rpc.core.bootstrap;

import com.darkblade.rpc.common.util.SpiSupportUtil;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.exception.LocalServerException;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.util.List;

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
        ServiceMetadataManager.getInstance().initalizeChannelFactory();
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
