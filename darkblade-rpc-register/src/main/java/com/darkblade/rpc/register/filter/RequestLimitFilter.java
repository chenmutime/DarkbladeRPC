package com.darkblade.rpc.register.filter;


import com.darkblade.rpc.common.dto.NrpcRequest;
import com.darkblade.rpc.register.annotation.Filter;
import com.darkblade.rpc.register.exception.FilterException;
import com.darkblade.rpc.register.limiter.RateLimiterHelper;

@Filter
public class RequestLimitFilter implements RequestFilter {

    @Override
    public void doFilter(NrpcRequest nrpcRequest) throws FilterException {
        if(!RateLimiterHelper.tryAcquire()){
            throw new FilterException("已超过最大请求数");
        }
    }

}
