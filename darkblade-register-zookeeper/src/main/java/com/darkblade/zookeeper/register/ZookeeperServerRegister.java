package com.darkblade.zookeeper.register;

import com.darkblade.rpc.common.constant.ZookeeperConstant;
import com.darkblade.rpc.register.registry.ServerRegister;
import com.darkblade.zookeeper.config.ZookeeperServerProperties;
import org.apache.zookeeper.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ZookeeperServerRegister implements ServerRegister {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private ZookeeperServerProperties zookeeperServerProperties;

    public ZookeeperServerRegister(ZookeeperServerProperties zookeeperServerProperties) {
        if (null == zookeeperServerProperties) {
            zookeeperServerProperties = new ZookeeperServerProperties();
        }
        this.zookeeperServerProperties = zookeeperServerProperties;
    }

    @Override
    public void register(String serviceAddress) {
        ZooKeeper zookeeper = connectZookeeper();
        addRootNode(zookeeper);
        creatNode(zookeeper, serviceAddress);
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
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return zookeeper;
    }

    private void addRootNode(ZooKeeper zooKeeper) {
        try {
            logger.info("Initiating root node...");
            if (null == zooKeeper.exists(ZookeeperConstant.ZK_ROOT_PATH, false)) {
                zooKeeper.create(ZookeeperConstant.ZK_ROOT_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }

    //    注册服务地址
    private void creatNode(ZooKeeper zk, String serviceAddress) {
        try {
            logger.info("Initiating service node...");
            String path = zk.create(ZookeeperConstant.ZK_DATA_PATH, serviceAddress.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            logger.info("create data node -> {}", path);
        } catch (KeeperException e) {
            logger.error(e.getMessage());
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }
    }
}
