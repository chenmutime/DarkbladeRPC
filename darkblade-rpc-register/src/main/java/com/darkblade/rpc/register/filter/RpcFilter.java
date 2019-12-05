package com.darkblade.rpc.register.filter;

import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.register.exception.RpcFilterException;

public interface RpcFilter {

    void doFilter(RpcRequest nrpcRequest) throws RpcFilterException;

}
