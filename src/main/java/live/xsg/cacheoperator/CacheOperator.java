package live.xsg.cacheoperator;

import live.xsg.cacheoperator.flusher.Refresher;

/**
 * 缓存刷新器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator {

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 ""
     * 2.缓存存在数据，则返回缓存中的旧数据
     *
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
