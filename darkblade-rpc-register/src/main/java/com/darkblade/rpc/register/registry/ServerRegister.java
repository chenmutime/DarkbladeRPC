package com.darkblade.rpc.register.registry;

import com.darkblade.rpc.register.config.RpcProperties;

public interface ServerRegister {

    void register(RpcProperties rpcProperties, String serviceAddress);
}
