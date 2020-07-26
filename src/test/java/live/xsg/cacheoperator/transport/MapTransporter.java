package live.xsg.cacheoperator.transport;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.transport.Transporter;

import java.util.HashMap;
import java.util.Map;

/**
 * 使用map存储数据
 * Created by xsg on 2020/7/26.
 */
public class MapTransporter implements Transporter {

    private final Map<String, Object> cache = new HashMap<>();

    @Override
    public synchronized String get(String key) {
        return (String) this.cache.get(key);
    }

    @Override
    public synchronized void set(String key, long expire, String value) {
        this.cache.put(key, value);
    }

    @Override
    public synchronized int setIfNotExist(String key, String value, long expire) {
        if (cache.containsKey(key)) {
            return Constants.RESULT_FAILURE;
        }
        this.cache.put(key, value);
        return Constants.RESULT_SUCCESS;
    }

    @Override
    public synchronized void del(String key) {
        this.cache.remove(key);
    }
}
