package com.darkblade.rpc.core.netty.handler;

import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.core.helper.ServiceMetadataManager;
import com.darkblade.rpc.core.context.RpcContext;
import com.darkblade.rpc.core.netty.pool.ChannelPoolFactory;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class NettyClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    //      临时存储RpcFuture，用于接收对应的响应。原本写在公共类里面，但怕出现内存泄漏
    private ConcurrentMap<String, RpcContext> pending_queued = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) throws Exception {
        if (null != rpcResponse) {
            RpcContext rpcContext = pending_queued.get(rpcResponse.getRequestId());
            if (null != rpcContext) {
                rpcContext.setResponse(rpcResponse);
                rpcContext.done();
                pending_queued.remove(rpcResponse.getRequestId());
                ChannelPoolFactory.getInstance().releaseChannel(rpcContext, ctx.channel());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage());
        for (RpcContext rpcContext : pending_queued.values()) {
            ChannelPoolFactory.getInstance().releaseChannel(rpcContext, ctx.channel());
        }
    }

    public void saveRpcContext(String requestId, RpcContext rpcContext) {
        pending_queued.put(requestId, rpcContext);
    }
}
