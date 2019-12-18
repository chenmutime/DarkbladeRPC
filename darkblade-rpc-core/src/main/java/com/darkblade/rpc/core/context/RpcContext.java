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

        /**
         * 当前线程尝试获取锁，没有获取到，会封装一个node扔到等待队列，之后会使用LockSupport.park使当前线程处于阻塞状态
         * @param permits
         * @return
         */
        @Override
        protected boolean tryAcquire(int permits) {
            return getState() == done;
        }

        /**
         * 当前线程释放锁，会取得等待队列里的第一个node，即当前线程，使用LockSupport.unpark方法释放当前线程的阻塞状态
         * @param permits
         * @return
         */
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
