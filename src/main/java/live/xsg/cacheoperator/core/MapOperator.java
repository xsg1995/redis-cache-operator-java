package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.flusher.Refresher;

import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * map类型操作接口
 * Created by xsg on 2020/8/17.
 */
public interface MapOperator {

    /**
     * 从缓存中获取数据，Map类型，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_MAP
     * @param key key
     * @return 返回缓存数据，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_MAP
     */
    Map<String, String> hgetAll(String key);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 Constants.EMPTY_MAP
     * 2.缓存存在数据，则返回缓存中的旧数据
     *
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回 空的HashMap 或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回 空的HashMap 或者缓存中的旧数据
     * 使用 Executor executor = Executors.newCachedThreadPool()
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回 空的HashMap 或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回 空的HashMap 或者缓存中的旧数据
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executorService 指定以线程池实现
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService);

    /**
     * map类型
     * 获取map类型某个字段的值
     * @param key key
     * @param field map中的key
     * @return map中对应字段的值
     */
    String hget(String key, String field);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在其他线程在刷新缓存中，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存不存在数据，则返回 Constants.EMPTY_STRING
     * 2.缓存存在数据，则返回缓存中的旧数据
     * @param key key
     * @param field map中的key
     * @param expire 过期时间 毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回 Constants.EMPTY_STRING 或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回 Constants.EMPTY_STRING 或者缓存中的旧数据
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param field map中的key
     * @param expire 过期时间 毫秒
     * @param fluster 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 如果当前没有其他线程在刷新缓存，则开启一个线程执行缓存刷新，当前线程返回 Constants.EMPTY_STRING 或者缓存中的旧数据
     * 如果当前已经有其他现在在刷新缓存，则当前线程返回 Constants.EMPTY_STRING 或者缓存中的旧数据
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param field map中的key
     * @param expire 过期时间 毫秒
     * @param fluster 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executorService 指定线程池实现
     * @return 返回缓存中的数据
     */
    String hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster, ExecutorService executorService);
}
