package com.darkblade.rpc.register.filter;


import com.darkblade.rpc.common.dto.RpcRequest;
import com.darkblade.rpc.register.exception.RpcFilterException;
import com.darkblade.rpc.register.limiter.RateLimiterHelper;

public class RequestLimitFilter implements RpcFilter {

    @Override
    public void doFilter(RpcRequest nrpcRequest) throws RpcFilterException {
        if(!RateLimiterHelper.tryAcquire()){
            throw new RpcFilterException("已超过最大请求数");
        }
    }

}
