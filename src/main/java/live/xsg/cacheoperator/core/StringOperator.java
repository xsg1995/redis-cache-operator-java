package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.flusher.Refresher;

import java.util.concurrent.ExecutorService;

/**
 * string类型操作接口
 * Created by xsg on 2020/8/17.
 */
public interface StringOperator {

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则调用Refresher获取最新缓存数据
     *
     * 刷新缓存时，控制只有一个线程可以执行，当存在线程正在刷新缓存，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存存在数据，则返回缓存中的数据
     * 2.缓存不存在数据，则在阻塞一定时间，等待缓存中有数据在返回，参数 blockTime 控制
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String get(String key, long expire, Refresher<String> flusher);

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 刷新缓存时，控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     * 使用 Executor executor = Executors.newCachedThreadPool()
     * Future<String> resultFuture = RedisCacheContext.getContext().getFuture(); 获取异步刷新缓存的结果
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String getAsync(String key, long expire, Refresher<String> flusher);

    /**
     * 字符串类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 刷新缓存时，控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     * Future<String> resultFuture = RedisCacheContext.getContext().getFuture(); 获取异步缓存结果
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executorService 指定以线程池实现
     * @return 返回缓存中的数据
     */
    String getAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService);

    /**
     * 删除键
     * @param key 键
     */
    void del(String key);
}
