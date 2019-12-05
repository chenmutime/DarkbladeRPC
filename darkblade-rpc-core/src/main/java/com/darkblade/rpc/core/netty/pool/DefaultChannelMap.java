package com.darkblade.rpc.core.netty.pool;

import com.darkblade.rpc.core.netty.handler.DefaultChannelPoolHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.*;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultChannelMap extends AbstractChannelPoolMap<InetSocketAddress, ChannelPool> {

    private final ConcurrentMap<InetSocketAddress, AbstractChannelPoolHandler> handlerMap = new ConcurrentHashMap<>();

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() << 1);

    @Override
    protected ChannelPool newPool(InetSocketAddress inetAddr) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.remoteAddress(inetAddr);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(eventLoopGroup);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_SNDBUF, 1024);
        bootstrap.option(ChannelOption.SO_RCVBUF, 1024);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000);

        AbstractChannelPoolHandler channelPoolHandler = handlerMap.get(inetAddr);
        if(null == channelPoolHandler){
            channelPoolHandler = new DefaultChannelPoolHandler();
        }
//    使用FixedChannelPool，超过最大连接数的会进入等待任务队列；使用SimpleChannelPool，不会有限制，一旦发现池中没有可用连接，则会直接试图去创建一个
        SimpleChannelPool pool = new FixedChannelPool(bootstrap, channelPoolHandler, 100);
        return pool;
    }
}
