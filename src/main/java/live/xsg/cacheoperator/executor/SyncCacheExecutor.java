package live.xsg.cacheoperator.executor;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * 同步执行任务，调用当前线程运行
 * Created by xsg on 2020/7/30.
 */
public class SyncCacheExecutor<T> implements CacheExecutor<T> {
    @Override
    public Future<T> executor(CacheTask<T> task) {
        return new FutureAdapter<>(new FutureTask<>(task));
    }
}
