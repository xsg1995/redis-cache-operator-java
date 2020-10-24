package live.xsg.cacheoperator.executor;

import live.xsg.cacheoperator.context.RedisCacheContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 异步执行任务
 * Created by xsg on 2020/7/30.
 */
public class AsyncCacheExecutor<T> implements CacheExecutor<T> {

    private ExecutorService executorService = ExecutorUtils.getExecutorService();

    public AsyncCacheExecutor() {
    }

    public AsyncCacheExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public Future<T> executor(CacheTask<T> task) {
        FutureAdapter<T> futureAdapter = new FutureAdapter<>(executorService.submit(task));
        RedisCacheContext.getContext().setFuture(futureAdapter);
        return futureAdapter;
    }

    static class ExecutorUtils {
        private static ExecutorService executorService = Executors.newCachedThreadPool();

        public static ExecutorService getExecutorService() {
            return executorService;
        }
    }
}
