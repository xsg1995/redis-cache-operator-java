package live.xsg.cacheoperator.executor;

import live.xsg.cacheoperator.context.RedisCacheContext;
import live.xsg.cacheoperator.exception.CacheExecutionException;

import java.util.concurrent.*;

/**
 * Created by xsg on 2020/10/24.
 */
public class FutureAdapter<V> implements Future<V> {

    //future 对象
    private Future<V> future;

    public FutureAdapter(Future<V> future) {
        this.future = future;
    }

    /**
     * 运行任务并且获取 Future对象
     * @param t 结果
     * @param <T> 类型
     * @return future对象
     */
    public static <T> FutureAdapter<T> runAndGetFuture(T t) {
        FutureTask<T> futureTask = new FutureTask<>(() -> t);
        futureTask.run();
        FutureAdapter<T> futureAdapter = new FutureAdapter<>(futureTask);
        RedisCacheContext.getContext().setFuture(futureAdapter);
        return futureAdapter;
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
