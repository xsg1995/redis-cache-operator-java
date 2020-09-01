package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.CollectionUtils;

import java.util.List;

/**
 * list类型操作实现
 * Created by xsg on 2020/8/28.
 */
public class RedisListOperator extends AbstractRedisOperator implements ListOperator {

    public RedisListOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    public List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
        return this.lrange(key, start, end, expire, flusher, new SyncCacheExecutor<>());
    }

    @Override
    public String lpop(String key, long expire, Refresher<List<String>> flusher) {
        List<String> lrange = this.lrange(key, 0, 0, expire, flusher, new SyncCacheExecutor<>());
        String res = null;
        if (!CollectionUtils.isEmpty(lrange)) {
            res = lrange.get(0);
        }
        return res;
    }

    @Override
    public String rpop(String key, long expire, Refresher<List<String>> flusher) {
        List<String> lrange = this.lrange(key, -1, -1, expire, flusher, new SyncCacheExecutor<>());
        String res = null;
        if (!CollectionUtils.isEmpty(lrange)) {
            res = lrange.get(0);
        }
        return res;
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
    private List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher, CacheExecutor<List<String>> cacheExecutor) {
        List<String> res = this.transporter.lrange(key, start, end);
        if (!CollectionUtils.isEmpty(res)) return res;

        //缓存过期获取缓存中无数据，刷新缓存
        res = cacheExecutor.executor(() -> this.doFillListCache(key, start, end, expire, flusher));
        return res;
    }

    /**
     * 获取list数据，插入到缓存中
     * @param key key
     * @param expire 缓存过期时间
     * @param flusher 获取数据接口
     * @return 缓存中的数据
     */
    @SuppressWarnings("unchecked")
    private List<String> doFillListCache(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
        boolean isLoading = false;
        try {
            //设置正在加载key对应的数据
            isLoading = this.isLoading(key);
            //isLoading=true，则已有其他线程在刷新数据
            if (isLoading) {
                List<String> list = (List<String>) this.blockIfNeed(key);
                if (CollectionUtils.isEmpty(list) && list == Constants.EMPTY_LIST) {
                    return this.transporter.lrange(key, start, end);
                }
                return list;
            }

            List<String> data = flusher.refresh();

            //将data封装为数组
            String[] strings = new String[data.size()];
            int len = data.size() - 1;
            for (int i = data.size() - 1; i >= 0; i--) {
                strings[len - i] = data.get(i);
            }

            long newExpire = this.getExtendExpire(expire);
            this.transporter.lpush(key, newExpire, strings);

            return this.transporter.lrange(key, start, end);
        } finally {
            if (!isLoading) {
                //设置key已经加载完毕
                this.loadFinish(key);
            }
        }
    }
}
