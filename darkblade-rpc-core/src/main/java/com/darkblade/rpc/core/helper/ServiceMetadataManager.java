package com.darkblade.rpc.core.helper;

import com.darkblade.rpc.core.context.RpcContext;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.netty.pool.DefaultChannelMap;
import io.netty.channel.Channel;
import io.netty.channel.pool.ChannelPool;
import org.springframework.util.SystemPropertyUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 保存来自服务端的metadata信息
 */
public class ServiceMetadataManager {

    //    服务列表
    private final List<String> SERVER_LIST = new ArrayList<>();
    //     以后还要处理负载均衡的问题
    private final ConcurrentMap<String, List<InetSocketAddress>> SOCKET_ADDRESS_CONCURRENT_MAP = new ConcurrentHashMap<>();

    private volatile static ServiceMetadataManager serviceMetadataManager;

    public static ServiceMetadataManager getInstance() {
        if (null == serviceMetadataManager) {
            synchronized (ServiceMetadataManager.class) {
                if (serviceMetadataManager == null) {
                    serviceMetadataManager = new ServiceMetadataManager();
                }
            }
        }
        return serviceMetadataManager;
    }

    public List<String> serverList() {
        return this.SERVER_LIST;
    }

    public List<InetSocketAddress> getInetSocketAddress(String serviceName) {
        return SOCKET_ADDRESS_CONCURRENT_MAP.get(serviceName);
    }

    public void updateConnectServer(List<String> serverList) throws RemoteServerException {

        clearServer();

        SERVER_LIST.addAll(serverList);
        for (String sockerAddr : serverList) {
            String[] addressArr = sockerAddr.split(SystemPropertyUtils.VALUE_SEPARATOR);
            if (addressArr == null || addressArr.length != 3) {
                throw new RemoteServerException("服务端配置错误：" + addressArr);
            }
            String serviceName = addressArr[0];
            String host = addressArr[1];
            int port = Integer.valueOf(addressArr[2]);
            InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
            if (SOCKET_ADDRESS_CONCURRENT_MAP.containsKey(serviceName)) {
                List<InetSocketAddress> serviceList = SOCKET_ADDRESS_CONCURRENT_MAP.get(serviceName);
                serviceList.add(inetSocketAddress);
            } else {
                List<InetSocketAddress> serviceList = new ArrayList<>();
                serviceList.add(inetSocketAddress);
                SOCKET_ADDRESS_CONCURRENT_MAP.put(serviceName, serviceList);
            }
        }
    }

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Lock writeLock = readWriteLock.writeLock();

    private void clearServer() {
        this.writeLock.lock();
        try {
            ServiceMetadataManager.getInstance().SERVER_LIST.clear();
            SOCKET_ADDRESS_CONCURRENT_MAP.clear();
        } finally {
            this.writeLock.unlock();
        }
    }

}
