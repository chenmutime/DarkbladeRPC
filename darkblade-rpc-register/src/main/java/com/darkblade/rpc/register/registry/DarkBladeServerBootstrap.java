package com.darkblade.rpc.register.registry;

import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.common.serializer.RpcDecoder;
import com.darkblade.rpc.common.serializer.RpcEncoder;
import com.darkblade.rpc.common.util.SpiSupportUtil;
import com.darkblade.rpc.register.annotation.RpcService;
import com.darkblade.rpc.register.config.RpcProperties;
import com.darkblade.rpc.register.filter.RpcFilter;
import com.darkblade.rpc.register.netty.handler.NettyServerHandler;
import com.darkblade.rpc.register.limiter.RateLimiterHelper;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.SystemPropertyUtils;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DarkBladeServerBootstrap extends RpcProperties implements ApplicationContextAware, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RpcProperties rpcProperties;
    private List<RpcFilter> rpcFilterList;

    public DarkBladeServerBootstrap(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
        RateLimiterHelper.init(rpcProperties.getLimiter());
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        register();
        loadAllRpcServices(ctx);
        loadAllRpcFilters(ctx);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        startupNettyServer();
    }

    /**
     * 向zookeeper注册自身
     */
    private synchronized void register() {
        List<ServerRegister> serverRegisterList = new SpiSupportUtil().loadSPI(ServerRegister.class);
        try {
            if (serverRegisterList.isEmpty()) {
                throw new Exception("you need to add a service register center");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ServerRegister serverRegister = serverRegisterList.get(0);
        String serviceName = rpcProperties.getServiceName();
        String serviceAddress = serviceName + SystemPropertyUtils.VALUE_SEPARATOR + rpcProperties.getHost() + SystemPropertyUtils.VALUE_SEPARATOR + rpcProperties.getPort();
        serverRegister.register(rpcProperties, serviceAddress);
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
        logger.info("Scanning NrpcServices...");
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(RpcService.class);
        try {
            for (Object obj : beanMap.values()) {
                String interfaceName = obj.getClass().getAnnotation(RpcService.class).value().getSimpleName();
                if (ServerManager.getBeanMap().containsKey(interfaceName)) {
                    throw new Exception("Classes with duplicates：" + interfaceName);
                }
                ServerManager.getBeanMap().put(interfaceName, obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startupNettyServer() {
        logger.info("starting server...");
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            io.netty.bootstrap.ServerBootstrap bootstrap = new io.netty.bootstrap.ServerBootstrap();
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
            logger.info("binded port : {}", rpcProperties.getPort());
//            同步绑定端口号
            ChannelFuture future = bootstrap.bind(rpcProperties.getPort()).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
