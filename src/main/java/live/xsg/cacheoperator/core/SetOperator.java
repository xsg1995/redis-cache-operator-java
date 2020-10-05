package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.flusher.Refresher;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * set 类型操作接口
 * Created by xsg on 2020/9/16.
 */
public interface SetOperator {

    /**
     * Set 类型
     * 从缓存中获取数据，如果缓存数据不存在或缓存过期，则刷新缓存数据
     * 刷新缓存时，控制只有一个线程可以刷新缓存，当存在线程正在刷新缓存，会有两种情况：
     * 1.缓存存在数据，则返回缓存中的数据
     * 2.缓存不存在数据，则在阻塞一定时间，等待缓存中有数据再返回，参数 blockTime 控制
     * @param key key
     * @param expire 缓存的过期时间，单位 毫秒
     * @param flusher 当缓存中无数据或者缓存过期时，刷新缓存数据的接口
     * @return 返回缓存中的数据
     */
    Set<String> smembers(String key, long expire, Refresher<Set<String>> flusher);

    /**
     * 当没有缓存中无数据或者缓存过期时，异步刷新缓存数据
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     *
     * 可以通过 Future<Set<String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存的过期时间，单位 毫秒
     * @param flusher 当缓存中无数据或者缓存过期时，刷新缓存数据的接口
     * @return 缓存有数据则返回数据；缓存无数据或者异步刷新，则返回 null
     */
    Set<String> smembersAsync(String key, long expire, Refresher<Set<String>> flusher);

    /**
     * 当没有缓存中无数据或者缓存过期时，异步刷新缓存数据
     * 该方法返回 null 时，说明缓存正在异步刷新，调用  RedisCacheContext.getContext().getFuture() 获取异步刷新结果
     *
     * 可以通过 Future<Set<String>> future = RedisCacheContext.getContext().getFuture(); 获取异步执行结果
     * @param key key
     * @param expire 缓存的过期时间，单位 毫秒
     * @param flusher 当缓存中无数据或者缓存过期时，刷新缓存数据的接口
     * @param executorService 自定义executor
     * @return 缓存有数据则返回数据；缓存无数据或者异步刷新，则返回 null
     */
    Set<String> smembersAsync(String key, long expire, Refresher<Set<String>> flusher, ExecutorService executorService);
}
