package com.darkblade.rpc.core.helper;

import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.core.context.RpcContext;
import com.darkblade.rpc.core.context.RpcContextBuilder;
import com.darkblade.rpc.core.exception.LocalServerException;
import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.netty.handler.NettyClientHandler;
import com.darkblade.rpc.core.netty.pool.ChannelPoolFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;

import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class SendMessageHelper {

    public static Optional<RpcContext> sendRequest(String serviceName, RpcRequest rpcRequest) throws RemoteServerException {
        RpcContext rpcContext = RpcContextBuilder.build(serviceName);
        ChannelPool channelPool = ChannelPoolFactory.getInstance().getChannelPool(rpcContext);
        try {
            if (null == channelPool) {
                throw new LocalServerException("channel pool is null");
            }
        } catch (LocalServerException e) {
            e.printStackTrace();
        }
        Future<Channel> future = channelPool.acquire();
        try {
            Channel channel = future.get();
            if (null != channel) {
                NettyClientHandler nettyClientHandler = channel.pipeline().get(NettyClientHandler.class);
                nettyClientHandler.saveRpcContext(rpcRequest.getRequestId(), rpcContext);
                channel.writeAndFlush(rpcRequest);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Optional.of(rpcContext);
    }
}
