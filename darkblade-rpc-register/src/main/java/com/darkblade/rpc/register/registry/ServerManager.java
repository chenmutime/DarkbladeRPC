package com.darkblade.rpc.register.registry;

import java.util.concurrent.*;

public class ServerManager {

    private final static ConcurrentMap<String, Object> BEAN_MAP = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, Object> getBeanMap(){
        return BEAN_MAP;
    }

    private static ThreadPoolExecutor poolExecutor;

    public static void submit(Runnable task) {
        if (poolExecutor == null) {
            synchronized (ServerManager.class) {
                if (poolExecutor == null) {
                    poolExecutor = new ThreadPoolExecutor(16, 16, 600L
                            , TimeUnit.SECONDS, new ArrayBlockingQueue<>(655536));
                }
            }
        }
        poolExecutor.submit(task);
    }

}
