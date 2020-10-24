package live.xsg.cacheoperator.exception;

/**
 * 缓存执行异常
 * Created by xsg on 2020/10/24.
 */
public class CacheExecutionException extends RuntimeException {

    public CacheExecutionException(String message) {
        super(message);
    }

    public CacheExecutionException(String message, Throwable e) {
        super(message, e);
    }
}
