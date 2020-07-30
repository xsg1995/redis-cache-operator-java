package live.xsg.cacheoperator.executor;

/**
 * 同步执行任务，调用当前线程运行
 * Created by xsg on 2020/7/30.
 */
public class SyncCacheExecutor implements CacheExecutor {
    @Override
    public Object executor(CacheTask task) {
        return task.run();
    }
}
