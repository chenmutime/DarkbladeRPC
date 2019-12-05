package com.darkblade.rpc.core.netty.handler;

import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.common.dto.RpcResponse;
import com.darkblade.rpc.common.serializer.RpcDecoder;
import com.darkblade.rpc.common.serializer.RpcEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.pool.AbstractChannelPoolHandler;

public class DefaultChannelPoolHandler extends AbstractChannelPoolHandler {

    @Override
    public void channelCreated(Channel channel) throws Exception {
        ChannelPipeline channelPipeline = channel.pipeline();
        channelPipeline.addLast(new RpcEncoder(RpcRequest.class));
        channelPipeline.addLast(new RpcDecoder(RpcResponse.class));
        channelPipeline.addLast(new NettyClientHandler());
    }

}
