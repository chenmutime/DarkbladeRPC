package com.darkblade.rpc.register.filter;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.register.exception.RpcFilterException;

public interface RpcFilter {

    void doFilter(NrpcRequest nrpcRequest) throws RpcFilterException;

}
