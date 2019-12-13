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
import io.netty.util.concurrent.GenericFutureListener;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class SendMessageHelper {

    public static Optional<RpcContext> sendRequest(String serviceName, RpcRequest rpcRequest) throws RemoteServerException {
        RpcContext rpcContext = RpcContextBuilder.build(serviceName);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ChannelPool channelPool = ChannelPoolFactory.getInstance().getChannelPool(rpcContext);
        try {
            if (null == channelPool) {
                throw new LocalServerException("channelPool is null");
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
                ChannelFuture channelFuture = channel.writeAndFlush(rpcRequest);
                channelFuture.addListener(new GenericFutureListener<Future<? super Void>>() {
                    @Override
                    public void operationComplete(Future<? super Void> future) throws Exception {
                        countDownLatch.countDown();
                    }
                });
                countDownLatch.await();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return Optional.of(rpcContext);
    }
}
