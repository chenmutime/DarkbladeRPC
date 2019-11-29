package com.darkblade.rpc.register.registry;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.common.dto.NrpcResponse;
import com.darkblade.rpc.common.serializer.RpcDecoder;
import com.darkblade.rpc.common.serializer.RpcEncoder;
import com.darkblade.rpc.register.annotation.Filter;
import com.darkblade.rpc.register.annotation.NrpcService;
import com.darkblade.rpc.register.config.RpcProperties;
import com.darkblade.rpc.register.filter.RequestFilter;
import com.darkblade.rpc.register.handler.NettyServerHandler;
import com.darkblade.rpc.register.limiter.RateLimiterHelper;
import com.darkblade.rpc.register.zookeeper.ZkServerRegister;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerBootstrap implements ApplicationContextAware, InitializingBean {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private RpcProperties rpcProperties;
    private List<RequestFilter> requestFilterList;

    public ServerBootstrap(RpcProperties rpcProperties) {
        this.rpcProperties = rpcProperties;
        RateLimiterHelper.init(rpcProperties.getLimiter());
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {

        register();
        try {
            loadAllNrpcServices(ctx);
            loadAllNrpcFilters(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        start();
    }

    /**
     * 向zookeeper注册自身
     */
    private synchronized void register() {
        ZkServerRegister zkServerRegister = new ZkServerRegister(rpcProperties);
        String serviceName = rpcProperties.getServiceName();
        zkServerRegister.register(serviceName + SystemPropertyUtils.VALUE_SEPARATOR + rpcProperties.getHost() + SystemPropertyUtils.VALUE_SEPARATOR + rpcProperties.getPort());
    }

    /**
     * 扫描所有过滤器
     *
     * @param ctx
     * @throws Exception
     */
    private synchronized void loadAllNrpcFilters(ApplicationContext ctx) throws Exception {
        logger.info("Scanning filters...");
        Map<String, Object> filterMap = ctx.getBeansWithAnnotation(Filter.class);
        List<RequestFilter> internalList = new ArrayList<>();
        for (Object obj : filterMap.values()) {
            if (obj instanceof RequestFilter) {
                internalList.add((RequestFilter) obj);
            }
        }
        if (null == requestFilterList) {
            requestFilterList = new CopyOnWriteArrayList(internalList);
        }

    }

    /**
     * 保存所有暴露的服务
     *
     * @param ctx
     * @throws Exception
     */
    private synchronized void loadAllNrpcServices(ApplicationContext ctx) throws Exception {
        logger.info("Scanning NrpcServices...");
        Map<String, Object> beanMap = ctx.getBeansWithAnnotation(NrpcService.class);
        for (Object obj : beanMap.values()) {
            String interfaceName = obj.getClass().getAnnotation(NrpcService.class).value().getSimpleName();
            if (ServerManager.getBeanMap().containsKey(interfaceName)) {
                throw new Exception("Classes with duplicates：" + interfaceName);
            }
            ServerManager.getBeanMap().put(interfaceName, obj);
        }
    }

    private void start() {
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
                    ch.pipeline().addLast(new RpcDecoder(NrpcRequest.class));
                    ch.pipeline().addLast(new RpcEncoder(NrpcResponse.class));
                    ch.pipeline().addLast(new NettyServerHandler(requestFilterList));
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
