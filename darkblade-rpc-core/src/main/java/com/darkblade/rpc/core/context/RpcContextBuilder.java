package com.darkblade.rpc.core.context;

import com.darkblade.rpc.core.exception.RemoteServerException;
import com.darkblade.rpc.core.helper.ServiceMetadataManager;

import java.net.InetSocketAddress;
import java.util.List;

public class RpcContextBuilder {

    public static RpcContext build(String serviceName) throws RemoteServerException {
        List<InetSocketAddress> serviceList = ServiceMetadataManager.getInstance().getInetSocketAddress(serviceName);
        if (serviceList == null || serviceList.isEmpty()) {
            throw new RemoteServerException("service is not exists");
        }
        int serviceIndex = (int) Thread.currentThread().getId() % serviceList.size();
        InetSocketAddress inetSocketAddress = serviceList.get(serviceIndex);
        if (null == inetSocketAddress) {
            throw new RemoteServerException("service is not exists");
        }
        RpcContext rpcContext = new RpcContext(serviceName, inetSocketAddress);
        return rpcContext;
    }
}
