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
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 刷新缓存时，控制只有一个线程可以刷新缓存，当存在线程正在刷新缓存，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存存在数据，则返回缓存中的数据
     * 2.缓存不存在数据，则在阻塞一定时间，等待缓存中有数据在返回，参数 blockTime 控制
     *
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     * 使用 Executor executor = Executors.newCachedThreadPool()
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executorService 指定以线程池实现
     * @return 返回缓存中的数据
     */
    Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则刷新缓存数据
     * 控制只有一个线程可以刷新缓存，当存在线程正在刷新缓存，如果其他线程请求缓存数据，会有两种情况：
     * 1.缓存存在数据，则返回缓存中的数据
     * 2.缓存不存在数据，则在阻塞一定时间，等待缓存中有数据在返回，参数 blockTime 控制
     * @param key key
     * @param field map中的key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param flusher 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     *
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param field map中的key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param fluster 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    String hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster);

    /**
     * map类型
     * 从缓存中获取数据，如果缓存数据不存在或者缓存过期，则异步刷新缓存数据
     * 控制只有一个线程可以刷新缓存
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     *
     * 可以通过 Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param field map中的key
     * @param expire 缓存不存在数据或者缓存过期时，填充缓存时的过期时间，单位毫秒
     * @param fluster 当缓存不存在或者缓存过期时，刷新缓存数据的接口
     * @param executorService 指定线程池实现
     * @return 返回缓存中的数据
     */
    String hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster, ExecutorService executorService);
}
