package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.flusher.Refresher;

import java.util.concurrent.Executor;

/**
 * mock降级
 * Created by xsg on 2020/8/6.
 */
public abstract class AbstractMockCacheOperator implements CacheOperator {

    @Override
    public String getString(String key, long expire, Refresher<String> flusher) {
        return null;
    }

    @Override
    public String getString(String key, long expire, Refresher<String> flusher, String defaultVal) {
        return null;
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher) {
        return null;
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, String defaultVal) {
        return null;
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor) {
        return null;
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor, String defaultVal) {
        return null;
    }

    @Override
    public String getString(String key) {
        return null;
    }
}
