package live.xsg.cacheoperator.executor;

import live.xsg.cacheoperator.common.Constants;

import java.util.concurrent.*;

/**
 * 异步执行任务
 * Created by xsg on 2020/7/30.
 */
public class AsyncCacheExecutor<T> implements CacheExecutor<T> {

    private Executor executor;

    public AsyncCacheExecutor() {
        this(Executors.newCachedThreadPool());
    }

    public AsyncCacheExecutor(Executor executor) {
        this.executor = executor;
    }

    @Override
    public T executor(CacheTask<T> task) {
        executor.execute(task::run);
        return (T) Constants.EMPTY_STRING;
    }
}
