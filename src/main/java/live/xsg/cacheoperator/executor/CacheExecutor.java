package live.xsg.cacheoperator.executor;

import java.util.concurrent.Future;

/**
 * 缓存任务执行器
 * Created by xsg on 2020/7/30.
 */
public interface CacheExecutor<T> {

    /**
     * 执行任务，返回 Future 对象
     * @param task 具体执行的任务
     * @return 任务的返回值，调用 future.get() 获取具体的返回值
     */
    Future<T> executor(CacheTask<T> task);
}
