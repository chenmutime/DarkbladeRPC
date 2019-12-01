package com.darkblade.rpc.core.discovery;

import com.darkblade.rpc.common.constant.ZookeeperConstant;
import com.darkblade.rpc.core.config.ServerProperties;
import com.darkblade.rpc.core.config.ZookeeperServerProperties;
import com.darkblade.rpc.core.server.ServiceMetadataManager;
import com.darkblade.rpc.core.exception.RemoteServerException;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZkServerDiscovery implements ServerDiscovery {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZookeeperServerProperties zookeeperServerProperties;

    @Override
    public void loadAllServices(ServerProperties serverProperties) {
        this.zookeeperServerProperties = (ZookeeperServerProperties) serverProperties;
        ZooKeeper zookeeper = connectZookeeper();
        loadAllServices(zookeeper);
    }

    /**
     * 监视所有节点的变动并加载所有服务地址到本地
     *
     * @param zookeeper
     */
    private void loadAllServices(ZooKeeper zookeeper) {
        logger.info("正在加载所有服务");
        try {
            List<String> nodes = zookeeper.getChildren(ZookeeperConstant.ZK_ROOT_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                        logger.info("服务已更新");
                        loadAllServices(zookeeper);
                    }
                }
            });
            List<String> serverList = new ArrayList<>();
            for (String node : nodes) {
                byte[] serverAddressByte = zookeeper.getData(ZookeeperConstant.ZK_ROOT_PATH + "/" + node, false, null);
                String serverAddress = new String(serverAddressByte);
                serverList.add(serverAddress);
            }
            ServiceMetadataManager.getInstance().updateConnectServer(serverList);
        } catch (KeeperException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        } catch (RemoteServerException e) {
            logger.error(e.getMessage());
        }
    }

    private ZooKeeper connectZookeeper() {
        ZooKeeper zookeeper = null;
        try {
            zookeeper = new ZooKeeper(zookeeperServerProperties.getHost(), zookeeperServerProperties.getSessionTimeout(), new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    logger.info("服务已建立");
                }
            });
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
        return zookeeper;
    }
}