package live.xsg.cacheoperator.executor;

import live.xsg.cacheoperator.exception.CacheExecutionException;

import java.util.concurrent.*;

/**
 * Created by xsg on 2020/10/24.
 */
public class FutureAdapter<V> implements Future<V> {

    //future 对象
    private Future<V> future;

    public FutureAdapter(V v) {
        this.future = new FutureTask<>(() -> v);
    }

    public FutureAdapter(Future<V> future) {
        this.future = future;
    }

    /**
     * 获取执行结果
     * @return 异常则抛出 CacheExecutionException
     */
    public V getData() {
        try {
            return this.future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new CacheExecutionException("get data error.", e);
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.future.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.future.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.future.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return this.future.get();
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.future.get(timeout, unit);
    }
}
