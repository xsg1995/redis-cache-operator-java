package live.xsg.cacheoperator;

import live.xsg.cacheoperator.flusher.Refresher;

/**
 * 缓存刷新器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator {

    /**
     * 从缓存中获取数据，字符串类型，当缓存不存在获取过期，则刷新缓存
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String loadString(String key, long expire, Refresher<String> flusher);

    /**
     * 从缓存中获取数据，字符串类型，如果缓存中无数据，则返回 Constants.EMPTY_STRING
     * @param key key
     * @return 返回缓存数据，如果缓存不存在数据，则返回 Constants.EMPTY_STRING
     */
    String getString(String key);
}
