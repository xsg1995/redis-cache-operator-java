package live.xsg.cacheoperator.core.redis;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.core.ListOperator;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.FutureAdapter;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * list类型操作实现
 * Created by xsg on 2020/8/28.
 */
public class RedisListOperator extends AbstractRedisOperator implements ListOperator {

    //异步任务执行器
    protected CacheExecutor<List<String>> asyncCacheExecutor = new AsyncCacheExecutor<>();

    public RedisListOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    public List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
        return this.lrange(key, start, end, expire, flusher, new SyncCacheExecutor<>()).getData();
    }

    @Override
    public String lpop(String key, long expire, Refresher<List<String>> flusher) {
        List<String> lrange = this.lrange(key, 0, 0, expire, flusher, new SyncCacheExecutor<>()).getData();
        String res = null;
        if (!CollectionUtils.isEmpty(lrange)) {
            res = lrange.get(0);
        }
        return res;
    }

    @Override
    public String rpop(String key, long expire, Refresher<List<String>> flusher) {
        List<String> lrange = this.lrange(key, -1, -1, expire, flusher, new SyncCacheExecutor<>()).getData();
        String res = null;
        if (!CollectionUtils.isEmpty(lrange)) {
            res = lrange.get(0);
        }
        return res;
    }

    @Override
    public Future<List<String>> lrangeAsync(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
        return this.lrange(key, start, end, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Future<List<String>> lrangeAsync(String key, long start, long end, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
        return this.lrange(key, start, end, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    public Future<List<String>> lpopAsync(String key, long expire, Refresher<List<String>> flusher) {
        return this.lrange(key, 0, 0, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Future<List<String>> lpopAsync(String key, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
        return  this.lrange(key, 0, 0, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    public Future<List<String>> rpopAsync(String key, long expire, Refresher<List<String>> flusher) {
        return  this.lrange(key, -1, -1, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Future<List<String>> rpopAsync(String key, long expire, Refresher<List<String>> flusher, ExecutorService executorService) {
        return  this.lrange(key, -1, -1, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    protected Object getDataIgnoreValid(String key) {
        boolean exists = this.transporter.exists(key);
        if (exists) {
            return Constants.EMPTY_LIST;
        }
        return null;
    }

    /**
     * 如果缓存中无数据，则刷新缓存数据
     * @param key key
     * @param start start
     * @param end end
     * @param expire 过期时间
     * @param flusher 获取缓存数据
     * @param cacheExecutor executor
     * @return 返回指定范围的数据
     */
    private FutureAdapter<List<String>> lrange(String key,
                                             long start,
                                             long end,
                                             long expire,
                                             Refresher<List<String>> flusher,
                                             CacheExecutor<List<String>> cacheExecutor) {
        List<String> res = this.transporter.lrange(key, start, end);
        if (!CollectionUtils.isEmpty(res)) return new FutureAdapter<>(res);

        //缓存过期获取缓存中无数据，刷新缓存
        res = cacheExecutor.executor(() -> this.fillCache(new FillCache<List<String>>() {
            @Override
            public String getKey() {
                return key;
            }

            @SuppressWarnings("unchecked")
            @Override
            public List<String> getIgnoreValidData() {
                List<String> list = (List<String>) blockIfNeed(key);
                if (CollectionUtils.isEmpty(list) && list == Constants.EMPTY_LIST) {
                    return transporter.lrange(key, start, end);
                }
                return list;
            }

            @Override
            public List<String> getCacheData() {
                //执行具体的获取缓存数据逻辑
                List<String> data = flusher.refresh();
                if (data == null) {
                    data = new ArrayList<>();
                }
                //将data封装为数组
                String[] strings = new String[data.size()];
                int len = data.size() - 1;
                for (int i = data.size() - 1; i >= 0; i--) {
                    strings[len - i] = data.get(i);
                }
                //对过期时间进行延长
                long newExpire = getExtendExpire(expire);
                //填充缓存
                transporter.lpush(key, newExpire, strings);
                return data;
            }
        }));

        return res;
    }
}
