package com.darkblade.rpc.register.registry;

import java.util.concurrent.*;

public class ServerManager {

    private final static ConcurrentMap<String, Object> BEAN_MAP = new ConcurrentHashMap<>();

    public static ConcurrentMap<String, Object> getBeanMap() {
        return BEAN_MAP;
    }

    private static ThreadPoolExecutor poolExecutor;

    public static void submit(Runnable task) {
        if (poolExecutor == null) {
            synchronized (ServerManager.class) {
                if (poolExecutor == null) {
                    int poolSzie = Runtime.getRuntime().availableProcessors();
                    poolExecutor = new ThreadPoolExecutor(poolSzie, poolSzie, 6000L
                            , TimeUnit.SECONDS, new ArrayBlockingQueue<>(Integer.MAX_VALUE));
                }
            }
        }
        poolExecutor.submit(task);
    }

}
