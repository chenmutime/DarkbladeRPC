package com.darkblade.rpc.core.server;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.context.RpcContext;
import com.darkblade.rpc.core.netty.handler.NettyClientHandler;
import com.darkblade.rpc.core.netty.pool.DefaultChannelMap;
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

/**
 * 保存来自服务端的metadata信息
 */
public class ServiceMetadataManager {

    //    服务列表
    private final List<String> SERVER_LIST = new ArrayList<>();
    //     以后还要处理负载均衡的问题
    private final ConcurrentMap<String, List<InetSocketAddress>> SOCKET_ADDRESS_CONCURRENT_MAP = new ConcurrentHashMap<>();
    //  管理着服务端的连接池
    private volatile AbstractChannelPoolMap defaultChannelMap;

    //    添加volatile为了禁止重排序，防止多线程修改nrpcServiceManager出错
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

    public void initalizeChannelFactory() {
        if (null == defaultChannelMap) {
            synchronized (ServiceMetadataManager.class) {
                if (defaultChannelMap == null) {
                    this.defaultChannelMap = new DefaultChannelMap();
                }
            }
        }
    }

    public void destoryConnections() {
        if (this.defaultChannelMap != null) {
            this.defaultChannelMap.close();
        }
        clearServer();
    }

    private Optional<RpcContext> createRpcContext(String serviceName) throws Exception {
        readLock.lock();
        try {
            List<InetSocketAddress> serviceList = SOCKET_ADDRESS_CONCURRENT_MAP.get(serviceName);
            if (serviceList == null || serviceList.isEmpty()) {
                throw new RemoteServerException("你调用的服务不存在");
            }
            int serviceIndex = (int) Thread.currentThread().getId() % serviceList.size();
            InetSocketAddress inetSocketAddress = serviceList.get(serviceIndex);
            if (null != inetSocketAddress) {
                RpcContext rpcContext = new RpcContext(serviceName, inetSocketAddress);
                return Optional.of(rpcContext);
            }
        } finally {
            readLock.unlock();
        }
        return Optional.empty();
    }

    private ChannelPool getChannelPool(RpcContext rpcContext) {
        return defaultChannelMap.get(rpcContext.getInetSocketAddress());
    }

    public void releaseChannel(RpcContext rpcContext, Channel channel) {
//        需要找到对应具体服务端的inetAddr才能获取ChannelPool
        ChannelPool channelPool = defaultChannelMap.get(rpcContext.getInetSocketAddress());
        channelPool.release(channel);
    }

    public static Optional<RpcContext> sendRequest(String serviceName, NrpcRequest nrpcRequest) throws Exception {
        Optional<RpcContext> rpcFutureOptional = ServiceMetadataManager.getInstance().createRpcContext(serviceName);
        if (rpcFutureOptional.isPresent()) {
            RpcContext rpcContext = rpcFutureOptional.get();
            CountDownLatch countDownLatch = new CountDownLatch(1);

            ChannelPool channelPool = getInstance().getChannelPool(rpcContext);
            Future<Channel> future = channelPool.acquire();
            Channel channel = future.get();
            if (null != channel) {
                NettyClientHandler nettyClientHandler = channel.pipeline().get(NettyClientHandler.class);
                nettyClientHandler.saveRpcFuture(nrpcRequest.getRequestId(), rpcContext);
                ChannelFuture channelFuture = channel.writeAndFlush(nrpcRequest);
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
            }
            return Optional.of(rpcContext);
        }
        return Optional.empty();
    }


}
