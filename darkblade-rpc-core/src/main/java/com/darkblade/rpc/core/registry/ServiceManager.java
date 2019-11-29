package com.darkblade.rpc.core.registry;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.core.config.ZookeeperProperties;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.future.RpcFuture;
import com.darkblade.rpc.core.handler.NettyClientHandler;
import com.darkblade.rpc.core.pool.DefaultChannelMap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.springframework.util.SystemPropertyUtils;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServiceManager {

    //    服务列表
    private final List<String> SERVER_LIST = new ArrayList<>();
    //     以后还要处理负载均衡的问题
    private final ConcurrentMap<String, List<InetSocketAddress>> SOCKET_ADDRESS_CONCURRENT_MAP = new ConcurrentHashMap<>();
    //  管理着服务端的连接池
    private volatile AbstractChannelPoolMap defaultChannelMap;

    //    添加volatile为了禁止重排序，防止多线程修改nrpcServiceManager出错
    private volatile static ServiceManager serviceManager;

    public static ServiceManager getInstance() {
        if (null == serviceManager) {
            synchronized (ServiceManager.class) {
                if (serviceManager == null) {
                    serviceManager = new ServiceManager();
                }
            }
        }
        return serviceManager;
    }

    public AbstractChannelPoolMap getDefaultChannelMap() {
        return this.defaultChannelMap;
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
    private Lock readLock = readWriteLock.readLock();
    private Lock writeLock = readWriteLock.writeLock();

    private void clearServer() {
        this.writeLock.lock();
        try {
            SERVER_LIST.clear();
            SOCKET_ADDRESS_CONCURRENT_MAP.clear();
            if (null != defaultChannelMap) {
                defaultChannelMap.forEach(inetAddr -> {
                    defaultChannelMap.remove(inetAddr);
                });
            }
        } finally {
            this.writeLock.unlock();
        }
    }

    public void initalizeChannelFactory(ZookeeperProperties zookeeperProperties) {
        if (null == defaultChannelMap) {
            synchronized (ServiceManager.class) {
                if (defaultChannelMap == null) {
                    this.defaultChannelMap = new DefaultChannelMap(zookeeperProperties);
                }
            }
        }
    }

    public void destoryConnections() {
        if (this.defaultChannelMap != null) {
            this.defaultChannelMap.close();
        }
    }

    private Optional<RpcFuture> createRpcFutrue(String serviceName) throws Exception {
        readLock.lock();
        try {
            List<InetSocketAddress> serviceList = SOCKET_ADDRESS_CONCURRENT_MAP.get(serviceName);
            if (serviceList == null || serviceList.isEmpty()) {
                throw new RemoteServerException("你调用的服务不存在");
            }
            int serviceIndex = (int) Thread.currentThread().getId() % serviceList.size();
            InetSocketAddress inetSocketAddress = serviceList.get(serviceIndex);
            if (null != inetSocketAddress) {
                RpcFuture rpcFuture = new RpcFuture(serviceName, inetSocketAddress);
                return Optional.of(rpcFuture);
            }
        } finally {
            readLock.unlock();
        }
        return Optional.empty();
    }

    private ChannelPool getChannelPool(RpcFuture rpcFuture){
        return defaultChannelMap.get(rpcFuture.getInetSocketAddress());
    }

    public void releaseChannel(RpcFuture rpcFuture, Channel channel) {
//        需要找到对应具体服务端的inetAddr才能获取ChannelPool
        ChannelPool channelPool = defaultChannelMap.get(rpcFuture.getInetSocketAddress());
        channelPool.release(channel);
    }

    public static Optional<RpcFuture>  sendRequest(String serviceName, NrpcRequest nrpcRequest) throws Exception {
        Optional<RpcFuture> rpcFutureOptional = ServiceManager.getInstance().createRpcFutrue(serviceName);
        if (rpcFutureOptional.isPresent()) {
            RpcFuture rpcFuture = rpcFutureOptional.get();
            CountDownLatch countDownLatch = new CountDownLatch(1);

            ChannelPool channelPool = getInstance().getChannelPool(rpcFuture);
            Future<Channel> future = channelPool.acquire();
            Channel channel = future.get();
            if (null != channel) {
                NettyClientHandler nettyClientHandler = channel.pipeline().get(NettyClientHandler.class);
                nettyClientHandler.saveRpcFuture(nrpcRequest.getRequestId(), rpcFuture);
                ChannelFuture channelFuture = channel.writeAndFlush(nrpcRequest);
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
            }
            return Optional.of(rpcFuture);
        }
        return Optional.empty();
    }


}
