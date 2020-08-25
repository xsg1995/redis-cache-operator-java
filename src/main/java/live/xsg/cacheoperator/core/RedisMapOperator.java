package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.MapCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.MapUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * map类型操作实现
 * Created by xsg on 2020/8/18.
 */
public class RedisMapOperator extends AbstractRedisOperator implements MapOperator {
    //异步任务执行器
    protected CacheExecutor<Map<String, String>> asyncCacheExecutor = new AsyncCacheExecutor();

    public RedisMapOperator(Transporter transporter, CacheOperator cacheOperator, ResourceLoader resourceLoader) {
        super(transporter, cacheOperator, resourceLoader);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        Map<String, String> map = this.transporter.hgetAll(key);
        MapCodec.MapData mapData = (MapCodec.MapData) this.getDecodeData(map, CodecEnum.MAP);
        boolean invalid = this.isInvalid(mapData.getActualExpireTime());

        if (invalid) {
            return Constants.EMPTY_MAP;
        }

        return mapData.getData();
    }

    @Override
    public Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.hgetAll(key, expire, flusher, new SyncCacheExecutor<>());
    }

    @Override
    public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.hgetAll(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService) {
        return this.hgetAll(key, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    public String hget(String key, String field) {
        String res = this.transporter.hget(key, field);
        if (res == null) {
            return Constants.EMPTY_STRING;
        }
        return res;
    }

    @Override
    public String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher) {
        String res = this.transporter.hget(key, field);
        if (res != null) {
            return res;
        }

        //缓存中无数据，则获取请求数据
        Map<String, String> data = this.hgetAll(key, expire, flusher);
        res = data.get(field);
        if (res == null) {
            res = Constants.EMPTY_STRING;
        }
        return res;
    }

    private Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher, CacheExecutor<Map<String, String>> cacheExecutor) {
        Map<String, String> resMap = this.transporter.hgetAll(key);

        //数据解码
        MapCodec.MapData mapData = (MapCodec.MapData) this.getDecodeData(resMap, CodecEnum.MAP);
        boolean invalid = this.isInvalid(mapData.getActualExpireTime());

        if (invalid) {
            //缓存过期获取缓存中无数据，刷新缓存
            resMap = cacheExecutor.executor(() -> this.doFillMapCache(key, expire, flusher));
        } else {
            //缓存中存在数据且未过期
            resMap = mapData.getData();
        }

        if (resMap == null) {
            resMap = new HashMap<>();
        }
        return resMap;
    }

    /**
     * 填充数据到缓存中
     * @param key key
     * @param expire 缓存过期时间
     * @param flusher 获取缓存数据
     * @return 返回最新数据
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> doFillMapCache(String key, long expire, Refresher<Map<String, String>> flusher) {
        boolean isLoading = false;
        try {
            isLoading = this.isLoading(key);
            //isLoading=true，则已有其他线程在刷新数据
            if (isLoading) {
                return (Map<String, String>) this.blockIfNeed(key);
            }

            Map<String, String> data = flusher.refresh();

            long newExpire = this.getExtendExpire(expire);
            MapCodec.MapData encodeMap = (MapCodec.MapData) this.getEncodeData(expire, data, CodecEnum.MAP);
            this.transporter.hset(key, newExpire, encodeMap.getData());

            return data;
        } finally {
            if (!isLoading) {
                //设置key已经加载完毕
                this.loadFinish(key);
            }
        }
    }

    @Override
    protected Object getDataIgnoreValid(String key) {
        //检查是否已经有其他线程刷新完缓存
        Map<String, String> cacheMap = this.transporter.hgetAll(key);
        if (MapUtils.isEmpty(cacheMap)) return null;

        MapCodec.MapData mapData = (MapCodec.MapData) this.getDecodeData(cacheMap, CodecEnum.MAP);
        return mapData.getData();
    }
}
