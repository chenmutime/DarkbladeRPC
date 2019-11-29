package com.darkblade.rpc.core.proxy;

import java.lang.reflect.Proxy;
import java.util.Optional;

public class ProxyBuidler {

    public static Optional<Object> build(Class<?> interfaceClass) {
        if (interfaceClass.isInterface()) {
            Object obj = Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new ObjectProxy(interfaceClass));
            return Optional.of(obj);
        }
        return Optional.empty();
    }
}
