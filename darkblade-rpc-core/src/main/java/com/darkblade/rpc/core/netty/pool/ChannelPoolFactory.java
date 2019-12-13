package com.darkblade.rpc.core.netty.pool;

import com.darkblade.rpc.core.context.RpcContext;
import com.darkblade.rpc.core.helper.ServiceMetadataManager;
import io.netty.channel.Channel;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;

public class ChannelPoolFactory {

    //  管理着服务端的连接池
    private volatile AbstractChannelPoolMap defaultChannelMap;

    private volatile static ChannelPoolFactory channelPoolFactory;

    public static ChannelPoolFactory getInstance() {
        if (null == channelPoolFactory) {
            synchronized (ChannelPoolFactory.class) {
                if (channelPoolFactory == null) {
                    channelPoolFactory = new ChannelPoolFactory();
                }
            }
        }
        return channelPoolFactory;
    }

    public void initalizeChannelFactory() {
        if (null == defaultChannelMap) {
            synchronized (ChannelPoolFactory.class) {
                if (defaultChannelMap == null) {
                    this.defaultChannelMap = new DefaultChannelMap();
                }
            }
        }
    }

    public void destoryConnections() {
        if (this.defaultChannelMap != null) {
            this.defaultChannelMap.close();
        }
    }


    public ChannelPool getChannelPool(RpcContext rpcContext) {
        return defaultChannelMap.get(rpcContext.getInetSocketAddress());
    }

    public void releaseChannel(RpcContext rpcContext, Channel channel) {
//        需要找到对应具体服务端的inetAddr才能获取ChannelPool
        ChannelPool channelPool = defaultChannelMap.get(rpcContext.getInetSocketAddress());
        channelPool.release(channel);
    }

}
