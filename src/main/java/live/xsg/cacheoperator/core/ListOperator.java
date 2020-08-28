package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.flusher.Refresher;

import java.util.List;

/**
 * list类型操作接口
 * Created by xsg on 2020/8/28.
 */
public interface ListOperator {

    /**
     * list类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 Constants.EMPTY_LIST
     * 2.缓存存在数据，则返回缓存中的旧数据
     * @param key key
     * @param start start
     * @param end end
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher);

    /**
     * list类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 Constants.EMPTY_STRING
     * 2.缓存存在数据，则返回缓存中的旧数据
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String lpop(String key, long expire, Refresher<List<String>> flusher);

    /**
     * list类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 Constants.EMPTY_STRING
     * 2.缓存存在数据，则返回缓存中的旧数据
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String rpop(String key, long expire, Refresher<List<String>> flusher);
}
