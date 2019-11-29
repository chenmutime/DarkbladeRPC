package com.darkblade.rpc.register.limiter;

import com.google.common.util.concurrent.RateLimiter;

public class RateLimiterHelper {

    private volatile static RateLimiter rateLimiter = null;

    public static synchronized void init(double permitsPerSecond) {
        rateLimiter = RateLimiter.create(permitsPerSecond);
    }

    public static boolean tryAcquire(){
        return rateLimiter.tryAcquire();
    }

}
