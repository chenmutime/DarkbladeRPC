package com.darkblade.rpc.register.handler;

import com.darkblade.rpc.common.constant.StatusCodeConstant;
import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.common.dto.NrpcResponse;
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

public class NettyServerHandler extends SimpleChannelInboundHandler<NrpcRequest> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private List<RpcFilter> rpcFilterList;

    public NettyServerHandler(List<RpcFilter> rpcFilterList) {
        this.rpcFilterList = rpcFilterList;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NrpcRequest nrpcRequest) throws Exception {
        logger.info("received a request, id is {}", nrpcRequest.getRequestId());
        try {
            for (RpcFilter filter : rpcFilterList) {
                filter.doFilter(nrpcRequest);
            }
        }catch (RpcFilterException e){
            NrpcResponse nrpcResponse = wrapResponse(StatusCodeConstant.TOO_MANY_REQUESTS, e.getMessage(), null, nrpcRequest.getRequestId());
            ctx.channel().writeAndFlush(nrpcResponse);
        }

        ServerManager.submit(new Runnable() {
            @Override
            public void run() {
                NrpcResponse nrpcResponse = new NrpcResponse();
                Channel channel = ctx.channel();
                try {
                    nrpcResponse = handle(nrpcRequest);
                } catch (InvocationTargetException e) {
                    logger.error("occur a exception:" + e);
                    nrpcResponse = wrapResponse(StatusCodeConstant.METHOD_NOT_EXIST, e.getCause().getMessage(), null, nrpcRequest.getRequestId());
                } catch (Exception e) {
                    logger.error("occur a exception:" + e);
                    nrpcResponse = wrapResponse(StatusCodeConstant.UNKOWN, e.getCause().getMessage(), null, nrpcRequest.getRequestId());
                } finally {
                    channel.writeAndFlush(nrpcResponse);
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

    private NrpcResponse handle(NrpcRequest nrpcRequest) throws InvocationTargetException {
        logger.info("执行服务方法");
        NrpcResponse nrpcResponse;
        if (!StringUtils.isEmpty(nrpcRequest.getRequestId())) {
            Object bean = ServerManager.getBeanMap().get(nrpcRequest.getCalssName());
            if (Objects.nonNull(bean)) {
                FastClass serviceFastClass = FastClass.create(bean.getClass());
                int methodIndex = serviceFastClass.getIndex(nrpcRequest.getMethodName(), nrpcRequest.getParameterTypes());
                if (methodIndex >= 0) {
                    Object result = serviceFastClass.invoke(methodIndex, bean, nrpcRequest.getParameters());
                    nrpcResponse = wrapResponse(StatusCodeConstant.SUCCESS, "", result, nrpcRequest.getRequestId());
                } else {
                    nrpcResponse = wrapResponse(StatusCodeConstant.METHOD_NOT_EXIST, "method is not exist", null, nrpcRequest.getRequestId());
                }
            } else {
                nrpcResponse = wrapResponse(StatusCodeConstant.SERVICE_NOT_EXIST, "service is not exist", null, nrpcRequest.getRequestId());
            }
        } else {
            nrpcResponse = wrapResponse(StatusCodeConstant.REQUEST_ID_IS_NULL, "requestId is null", null, nrpcRequest.getRequestId());
        }
        return nrpcResponse;
    }

    private NrpcResponse wrapResponse(int code, String error, Object body, String requestId) {
        NrpcResponse response = new NrpcResponse();
        response.setRequestId(requestId);
        response.setCode(code);
        response.setBody(body);
        response.setError(error);
        return response;
    }
}
