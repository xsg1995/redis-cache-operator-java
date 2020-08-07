package live.xsg.cacheoperator.exception;

/**
 * Resource读取错误
 * Created by xsg on 2020/6/12.
 */
public class LoadResourceException extends RuntimeException {

    public LoadResourceException(String message) {
        super(message);
    }

    public LoadResourceException(String message, Throwable e) {
        super(message, e);
    }
}
