package live.xsg.cacheoperator.exception;

/**
 * 重试恢复异常
 * Created by xsg on 2020/8/6.
 */
public class RetryRecoverException extends Exception {

    public RetryRecoverException(String message) {
        super(message);
    }

    public RetryRecoverException(String message, Throwable e) {
        super(message, e);
    }
}
