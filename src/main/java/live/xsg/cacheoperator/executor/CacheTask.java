package live.xsg.cacheoperator.executor;

/**
 * 缓存任务的封装，执行run方法
 * Created by xsg on 2020/7/30.
 */
public interface CacheTask<T> {
    /**
     * 运行具体的任务逻辑
     * @return 返回任务的执行结果
     */
    T run();
}
