package com.darkblade.rpc.register.netty.handler;

import com.darkblade.rpc.common.constant.StatusCodeConstant;
import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.register.registry.ServerManager;
import com.darkblade.rpc.register.exception.RpcFilterException;
import com.darkblade.rpc.register.filter.RpcFilter;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Objects;

public class NettyServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<RpcFilter> rpcFilterList;

    public NettyServerHandler(List<RpcFilter> rpcFilterList) {
        this.rpcFilterList = rpcFilterList;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) throws Exception {
        logger.info("received a request, id is {}", rpcRequest.getRequestId());
        try {
            for (RpcFilter filter : rpcFilterList) {
                filter.doFilter(rpcRequest);
            }
        }catch (RpcFilterException e){
            RpcResponse rpcResponse = wrapResponse(StatusCodeConstant.TOO_MANY_REQUESTS, e.getMessage(), null, rpcRequest.getRequestId());
            ctx.channel().writeAndFlush(rpcResponse);
        }

        ServerManager.submit(new Runnable() {
            @Override
            public void run() {
                RpcResponse rpcResponse = new RpcResponse();
                Channel channel = ctx.channel();
                try {
                    rpcResponse = handle(rpcRequest);
                } catch (InvocationTargetException e) {
                    logger.error("occur a exception:" + e);
                    rpcResponse = wrapResponse(StatusCodeConstant.METHOD_NOT_EXIST, e.getCause().getMessage(), null, rpcRequest.getRequestId());
                } catch (Exception e) {
                    logger.error("occur a exception:" + e);
                    rpcResponse = wrapResponse(StatusCodeConstant.UNKOWN, e.getCause().getMessage(), null, rpcRequest.getRequestId());
                } finally {
                    channel.writeAndFlush(rpcResponse);
                }
            }
        });

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("actived a channel");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("inactived a channel");
    }

    private RpcResponse handle(RpcRequest RpcRequest) throws InvocationTargetException {
        logger.info("执行服务方法");
        RpcResponse rpcResponse;
        if (!StringUtils.isEmpty(RpcRequest.getRequestId())) {
            Object bean = ServerManager.getBeanMap().get(RpcRequest.getCalssName());
            if (Objects.nonNull(bean)) {
                FastClass serviceFastClass = FastClass.create(bean.getClass());
                int methodIndex = serviceFastClass.getIndex(RpcRequest.getMethodName(), RpcRequest.getParameterTypes());
                if (methodIndex >= 0) {
                    Object result = serviceFastClass.invoke(methodIndex, bean, RpcRequest.getParameters());
                    rpcResponse = wrapResponse(StatusCodeConstant.SUCCESS, "", result, RpcRequest.getRequestId());
                } else {
                    rpcResponse = wrapResponse(StatusCodeConstant.METHOD_NOT_EXIST, "method is not exist", null, RpcRequest.getRequestId());
                }
            } else {
                rpcResponse = wrapResponse(StatusCodeConstant.SERVICE_NOT_EXIST, "service is not exist", null, RpcRequest.getRequestId());
            }
        } else {
            rpcResponse = wrapResponse(StatusCodeConstant.REQUEST_ID_IS_NULL, "requestId is null", null, RpcRequest.getRequestId());
        }
        return rpcResponse;
    }

    private RpcResponse wrapResponse(int code, String error, Object body, String requestId) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(requestId);
        rpcResponse.setCode(code);
        rpcResponse.setBody(body);
        rpcResponse.setError(error);
        return rpcResponse;
    }
}
