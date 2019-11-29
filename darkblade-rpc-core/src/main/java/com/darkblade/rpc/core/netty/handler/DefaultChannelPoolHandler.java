package com.darkblade.rpc.core.netty.handler;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.common.dto.NrpcResponse;
import com.darkblade.rpc.common.serializer.RpcDecoder;
import com.darkblade.rpc.common.serializer.RpcEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;

public class DefaultChannelPoolHandler extends AbstractChannelPoolHandler {


    @Override
    public void channelCreated(Channel channel) throws Exception {
        ChannelPipeline channelPipeline = channel.pipeline();
        channelPipeline.addLast(new RpcEncoder(NrpcRequest.class));
        channelPipeline.addLast(new RpcDecoder(NrpcResponse.class));
        channelPipeline.addLast(new NettyClientHandler());
    }

}
