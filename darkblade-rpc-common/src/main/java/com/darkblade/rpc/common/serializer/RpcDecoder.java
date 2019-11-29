package com.darkblade.rpc.common.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        byteBuf.markReaderIndex();
        int dateLen = byteBuf.readInt();
        if (byteBuf.readableBytes() < dateLen) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dateLen];
        byteBuf.readBytes(data);
        Object obj = ProtostuffUtils.deserialize(data, genericClass);
        list.add(obj);
    }
}
