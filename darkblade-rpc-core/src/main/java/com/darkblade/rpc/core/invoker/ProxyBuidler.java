package com.darkblade.rpc.core.invoker;

import com.darkblade.rpc.core.annotation.RpcClient;

import java.lang.reflect.Proxy;
import java.util.Optional;

public class ProxyBuidler {

    public static Optional<Object> build(Class<?> interfaceClass, RpcClient rpcClient) {
        if (interfaceClass.isInterface()) {
            Object obj = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new ObjectProxy(interfaceClass, rpcClient));
            return Optional.of(obj);
        }
        return Optional.empty();
    }
}
