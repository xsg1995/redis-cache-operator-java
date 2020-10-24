package live.xsg.cacheoperator.executor;

import java.util.concurrent.Callable;

/**
 * 缓存任务的封装，执行run方法
 * Created by xsg on 2020/7/30.
 */
public interface CacheTask<T> extends Callable<T> {
}
