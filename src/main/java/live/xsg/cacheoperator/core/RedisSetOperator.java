package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.CollectionUtils;

import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * set 类型操作实现
 * Created by xsg on 2020/9/16.
 */
public class RedisSetOperator extends AbstractRedisOperator implements SetOperator {

    //异步任务执行器
    protected CacheExecutor<Set<String>> asyncCacheExecutor = new AsyncCacheExecutor<>();

    public RedisSetOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    protected Object getDataIgnoreValid(String key) {
        Set<String> smembers = this.transporter.smembers(key);
        if (CollectionUtils.isEmpty(smembers)) return null;

        return smembers;
    }

    @Override
    public Set<String> smembers(String key, long expire, Refresher<Set<String>> flusher) {
        return this.smembers(key, expire, flusher, new SyncCacheExecutor<>());
    }

    @Override
    public Set<String> smembersAsync(String key, long expire, Refresher<Set<String>> flusher) {
        return this.smembers(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Set<String> smembersAsync(String key, long expire, Refresher<Set<String>> flusher, ExecutorService executorService) {
        return this.smembers(key, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    private Set<String> smembers(String key, long expire, Refresher<Set<String>> flusher, CacheExecutor<Set<String>> cacheExecutor) {
        Set<String> smembers = this.transporter.smembers(key);
        if (!CollectionUtils.isEmpty(smembers)) return smembers;

        smembers = cacheExecutor.executor(() -> this.doFillSetCache(key, expire, flusher));

        return smembers;
    }

    @SuppressWarnings("unchecked")
    private Set<String> doFillSetCache(String key, long expire, Refresher<Set<String>> flusher) {
        boolean lock = false;
        try {
            //获取锁
            lock = this.tryLock(key);
            if (!lock) {
                //没有获取到锁，走阻塞降级策略
                return (Set<String>) this.blockIfNeed(key);
            }

            //执行具体的获取缓存数据逻辑
            Set<String> data = flusher.refresh();
            String[] members = new String[data.size()];
            data.toArray(members);
            //对过期时间进行延长
            long newExpire = this.getExtendExpire(expire);
            //填充缓存
            this.transporter.sadd(key, newExpire, members);
            return data;
        } finally {
            if (!lock) {
                //设置key已经加载完毕
                this.unlock(key);
            }
        }
    }
}
