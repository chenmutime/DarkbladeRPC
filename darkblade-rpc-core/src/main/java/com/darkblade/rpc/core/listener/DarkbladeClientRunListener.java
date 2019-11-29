package com.darkblade.rpc.core.listener;

import com.darkblade.rpc.core.server.ServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplicationRunListener;

/**
 * 初始化服务
 */
public class DarkbladeClientRunListener implements SpringApplicationRunListener {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void starting() {
        logger.info("正在启动rpc客户端");
        start();
    }

    /**
     * 初始化连接池
     */
    private void start() {
        ServiceManager.getInstance().initalizeChannelFactory();
    }

}
