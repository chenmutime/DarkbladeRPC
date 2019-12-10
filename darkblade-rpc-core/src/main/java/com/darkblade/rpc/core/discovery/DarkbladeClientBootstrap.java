package com.darkblade.rpc.core.discovery;

import com.darkblade.rpc.common.util.SpiSupportUtil;
import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;

/**
 * 初始化调用连接池、服务中心
 */
public class DarkbladeClientBootstrap implements ApplicationContextAware {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
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
    private ZookeeperServerProperties zookeeperProperties;

    /**
     * 启动注册中心
     */
    private void startupServiceCenter() {
        List<ServerDiscovery> serverDiscoveryList = new SpiSupportUtil().loadSPI(ServerDiscovery.class);
        try {
            if (serverDiscoveryList.isEmpty()) {
                throw new Exception("you need to add a service discovery center");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerDiscovery serverDiscovery = serverDiscoveryList.get(0);
        serverDiscovery.startup(zookeeperProperties);
    }
}
