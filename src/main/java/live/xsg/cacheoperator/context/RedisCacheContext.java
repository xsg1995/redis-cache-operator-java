package live.xsg.cacheoperator.context;

import java.util.concurrent.Future;

/**
 * redis cache 上下文
 * Created by xsg on 2020/8/19.
 */
public class RedisCacheContext {

    private static ThreadLocal<RedisCacheContext> LOCAL = ThreadLocal.withInitial(RedisCacheContext::new);

    private Future<?> future;

    private RedisCacheContext() {}

    /**
     * 获取上下文
     * @return RedisCacheContext
     */
    public static RedisCacheContext getContext() {
        return LOCAL.get();
    }

    /**
     * 设置 future
     */
    public void setFuture(Future<?> future) {
        this.future = future;
    }

    /**
     * 获取future
     * @return future
     */
    @SuppressWarnings("unchecked")
    public <T> Future<T> getFuture() {
        return (Future<T>) future;
    }
}
