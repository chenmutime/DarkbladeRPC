package com.darkblade.rpc.register.bootstrap;

import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.common.serializer.RpcDecoder;
import com.darkblade.rpc.common.serializer.RpcEncoder;
import com.darkblade.rpc.register.annotation.RpcService;
import com.darkblade.rpc.register.config.NettyProperties;
import com.darkblade.rpc.register.filter.RpcFilter;
import com.darkblade.rpc.register.netty.handler.NettyServerHandler;
import com.darkblade.rpc.register.limiter.RateLimiterHelper;
import com.darkblade.rpc.register.registry.ServerManager;
import com.darkblade.rpc.register.registry.ServerRegister;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.SystemPropertyUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DarkBladeServerBootstrap extends NettyProperties implements ApplicationContextAware, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private NettyProperties nettyProperties;
    private List<RpcFilter> rpcFilterList;

    public DarkBladeServerBootstrap(NettyProperties nettyProperties) {
        this.nettyProperties = nettyProperties;
        RateLimiterHelper.init(nettyProperties.getLimiter());
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        register();
        loadAllRpcServices(ctx);
        loadAllRpcFilters(ctx);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        之所以单独开一个异步子线程，是因为future.channel().closeFuture().sync();这行代码会让主线程一直阻塞下去，导致http无法请求controller
        new Thread(new Runnable() {
            @Override
            public void run() {
                startupNettyServer();
            }
        }).start();

    }

    @Autowired
    private ServerRegister serverRegister;

    /**
     * 向zookeeper注册自身
     */
    private synchronized void register() {
        String serviceName = nettyProperties.getServiceName();
        String serviceAddress = serviceName + SystemPropertyUtils.VALUE_SEPARATOR + nettyProperties.getHost() + SystemPropertyUtils.VALUE_SEPARATOR + nettyProperties.getPort();
        serverRegister.register(serviceAddress);
    }

    /**
     * 扫描所有过滤器
     *
     * @param ctx
     * @throws Exception
     */
    private synchronized void loadAllRpcFilters(ApplicationContext ctx) {
        logger.info("Scanning filters...");
        String[] filters = ctx.getBeanNamesForType(RpcFilter.class);
        List<RpcFilter> filterList = new ArrayList<>();
        try {
            for (String filter : filters) {
                RpcFilter rpcFilter = (RpcFilter) Class.forName(filter).newInstance();
                filterList.add(rpcFilter);
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (null == rpcFilterList) {
            rpcFilterList = new CopyOnWriteArrayList(filterList);
        }

    }

    /**
     * 保存所有暴露的服务
     *
     * @param ctx
     * @throws Exception
     */
    private synchronized void loadAllRpcServices(ApplicationContext ctx) {
        logger.info("Scanning RpcServices...");
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(RpcService.class);
        try {
            for (Object obj : beanMap.values()) {
                Class<?>[] interfaces = obj.getClass().getInterfaces();
                if (interfaces.length > 0) {
                    Class interfaceClass = interfaces[0];
                    String interfaceName = interfaceClass.getSimpleName();
                    if (ServerManager.getBeanMap().containsKey(interfaceName)) {
                        throw new Exception("Classes with duplicates：" + interfaceName);
                    }
                    ServerManager.getBeanMap().put(interfaceName, obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startupNettyServer() {
        logger.info("starting netty server...");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RpcDecoder(RpcRequest.class));
                    ch.pipeline().addLast(new RpcEncoder(RpcResponse.class));
                    ch.pipeline().addLast(new NettyServerHandler(rpcFilterList));
                }
            });
            logger.info("netty has bound a port : {}", nettyProperties.getPort());
//            绑定端口号，等待服务器启动完毕，才会进入下行代码
            ChannelFuture future = bootstrap.bind(nettyProperties.getPort()).sync();
//          阻塞，知直到服务端关闭socket
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
