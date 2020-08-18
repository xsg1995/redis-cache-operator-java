package live.xsg.cacheoperator.executor;

/**
 * 同步执行任务，调用当前线程运行
 * Created by xsg on 2020/7/30.
 */
public class SyncCacheExecutor<T> implements CacheExecutor<T> {
    @Override
    public T executor(CacheTask<T> task) {
        return task.run();
    }
}
