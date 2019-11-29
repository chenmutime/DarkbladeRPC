package com.darkblade.rpc.core.future;

import com.darkblade.rpc.common.dto.NrpcResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

public class RpcFuture implements Future<Object> {

    private InetSocketAddress inetSocketAddress;
    private Sync sync;
    private String serviceName;
    private NrpcResponse response;

    public String getServiceName() {
        return serviceName;
    }

    public RpcFuture(String serviceName, InetSocketAddress inetSocketAddress) {
        this.sync = new Sync();
        this.serviceName = serviceName;
        this.inetSocketAddress = inetSocketAddress;
    }

    public void setResponse(NrpcResponse response) {
        this.response = response;
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
        return isDone();
    }

    public void done() {
        this.sync.release(1);
    }

    public InetSocketAddress getInetSocketAddress() {
        return inetSocketAddress;
    }

    public void setInetSocketAddress(InetSocketAddress inetSocketAddress) {
        this.inetSocketAddress = inetSocketAddress;
    }

    @Override
    public NrpcResponse get() {
        this.sync.acquire(-1);
        return this.response;
    }

    @Override
    public NrpcResponse get(long timeout, TimeUnit unit) throws InterruptedException {
        this.sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        return this.response;
    }

    class Sync extends AbstractQueuedSynchronizer {

        private final int done = 1;
        private final int pending = 0;

        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        protected boolean isDone() {
            getState();
            return getState() == done;
        }
    }
}
