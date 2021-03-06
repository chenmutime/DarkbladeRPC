package com.darkblade.core.zookeeper.discovery;

import com.darkblade.core.zookeeper.config.ZookeeperServerProperties;
import com.darkblade.rpc.common.constant.ZookeeperConstant;
import com.darkblade.rpc.core.discovery.ServerDiscovery;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.helper.ServiceMetadataManager;
import com.darkblade.rpc.core.netty.pool.ChannelPoolFactory;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ZookeeperServerDiscovery implements ServerDiscovery {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZookeeperServerProperties zookeeperServerProperties;

    private volatile ZooKeeper zooKeeper;

    public ZookeeperServerDiscovery(ZookeeperServerProperties zookeeperServerProperties) {
        if (null == zookeeperServerProperties) {
            zookeeperServerProperties = new ZookeeperServerProperties();
        }
        this.zookeeperServerProperties = zookeeperServerProperties;
    }

    @Override
    public void startup() {
        this.zooKeeper = connectZookeeper();
        loadAllServices(this.zooKeeper);
    }


    @Override
    public List<String> serviceNames() {
        return ServiceMetadataManager.getInstance().serverList();
    }

    @Override
    public String health() {
        if (this.zooKeeper.getState().isAlive()) {
            return "UP";
        }
        return "CLOESD";
    }

    @Override
    public void close() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            this.logger.error(e.toString());
        }
        ChannelPoolFactory.getInstance().destoryConnections();
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
