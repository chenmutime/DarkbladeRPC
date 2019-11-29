package com.darkblade.rpc.register.filter;

import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.register.exception.FilterException;

public interface RequestFilter {

    void doFilter(NrpcRequest nrpcRequest) throws FilterException;

}
