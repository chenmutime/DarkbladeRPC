package com.darkblade.rpc.common.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder {

    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * 在执行编码之前，netty会先分配一块内存（直接内存或堆内存），即ByteBuf，用于填充编码后的数据。如果是堆内存，则会发生一次数据拷贝操作。
     * @param channelHandlerContext
     * @param obj
     * @param byteBuf
     * @throws Exception
     */
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object obj, ByteBuf byteBuf) throws Exception {
        if(genericClass.isInstance(obj)){
            byte[] data = ProtostuffUtils.serialize(obj);
            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
        }
    }
}
