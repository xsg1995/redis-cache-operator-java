package live.xsg.cacheoperator.executor;

/**
 * 缓存任务执行器
 * Created by xsg on 2020/7/30.
 */
public interface CacheExecutor<T> {

    /**
     * 执行任务，运行CacheTask的run方法
     * @param task 具体执行的任务
     * @return 任务的返回值
     */
    T executor(CacheTask<T> task);
}
