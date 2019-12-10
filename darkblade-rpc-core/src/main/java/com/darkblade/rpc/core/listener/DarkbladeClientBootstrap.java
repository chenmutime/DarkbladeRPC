package com.darkblade.rpc.core.listener;

import com.darkblade.rpc.common.util.SpiSupportUtil;
import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * 初始化调用连接池、服务中心
 */
public class DarkbladeClientBootstrap implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void afterPropertiesSet() throws Exception {
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
