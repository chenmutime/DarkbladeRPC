package com.darkblade.rpc.core.context;

import com.darkblade.rpc.common.dto.RpcResponse;
import lombok.Data;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

@Data
public class RpcContext implements Future<Object> {

    private InetSocketAddress inetSocketAddress;
    private Sync sync;
    private String serviceName;
    private RpcResponse response;

    public RpcContext(String serviceName, InetSocketAddress inetSocketAddress) {
        this.sync = new Sync();
        this.serviceName = serviceName;
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    public void done() {
        this.sync.release(-1);
    }

    @Override
    public RpcResponse get() {
        this.sync.acquire(-1);
        return this.response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        this.sync.tryAcquireNanos(1, unit.toNanos(timeout));
        return this.response;
    }

    class Sync extends AbstractQueuedSynchronizer {

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int permits) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int permits) {
            if (getState() == pending && compareAndSetState(pending, done)) {
                return true;
            }
            return false;
        }

        protected boolean isDone() {
            return getState() == done;
        }
    }
}
