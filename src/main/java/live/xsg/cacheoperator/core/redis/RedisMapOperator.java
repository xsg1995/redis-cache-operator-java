package live.xsg.cacheoperator.core.redis;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.data.MapData;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.core.MapOperator;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.FutureAdapter;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.MapUtils;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * map类型操作实现
 * Created by xsg on 2020/8/18.
 */
public class RedisMapOperator extends AbstractRedisOperator implements MapOperator {
    //异步任务执行器
    protected CacheExecutor<Map<String, String>> asyncCacheExecutor = new AsyncCacheExecutor<Map<String, String>>();

    public RedisMapOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    public Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.hgetAll(key, expire, flusher, new SyncCacheExecutor<>()).getData();
    }

    @Override
    public Future<Map<String, String>> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.hgetAll(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Future<Map<String, String>> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService) {
        return this.hgetAll(key, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    public String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher) {
        Map<String, String> decodeMap = this.getDecodeData(key, field);

        if (Constants.EMPTY_MAP.equals(decodeMap) || decodeMap == null) {
            //缓存数据已经过期
            Map<String, String> data = this.hgetAll(key, expire, flusher);
            if (data == null) return null;

            return data.get(field);
        }

        return decodeMap.get(field);
    }

    @Override
    public Future<String> hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster) {
        Map<String, String> decodeMap = this.getDecodeData(key, field);

        if (Constants.EMPTY_MAP.equals(decodeMap) || decodeMap == null) {
            //缓存数据已经过期
            this.hgetAllAsync(key, expire, fluster);

            return null;
        }

        return new FutureAdapter<>(decodeMap.get(field));
    }

    @Override
    public Future<String> hgetAsync(String key, String field, long expire, Refresher<Map<String, String>> fluster, ExecutorService executorService) {
        Map<String, String> decodeMap = this.getDecodeData(key, field);

        if (Constants.EMPTY_MAP.equals(decodeMap) || decodeMap == null) {
            //缓存数据已经过期
            this.hgetAllAsync(key, expire, fluster, executorService);

            return null;
        }

        return new FutureAdapter<>(decodeMap.get(field));
    }

    private FutureAdapter<Map<String, String>> hgetAll(String key,
                                                       long expire,
                                                       Refresher<Map<String, String>> flusher,
                                                       CacheExecutor<Map<String, String>> cacheExecutor) {
        Map<String, String> resMap = this.transporter.hgetAll(key);

        //数据解码
        MapData mapData = (MapData) this.getDecodeData(resMap, CodecEnum.MAP);
        boolean invalid = mapData.isInvalid();

        if (invalid) {
            //缓存过期获取缓存中无数据，刷新缓存
            resMap = cacheExecutor.executor(() -> this.fillCache(new FillCache<Map<String, String>>() {
                @Override
                public String getKey() {
                    return key;
                }

                @SuppressWarnings("unchecked")
                @Override
                public Map<String, String> getIgnoreValidData() {
                    return (Map<String, String>) blockIfNeed(key);
                }

                @Override
                public Map<String, String> getCacheData() {
                    //执行具体的获取缓存数据逻辑
                    Map<String, String> data = flusher.refresh();
                    //对过期时间进行延长
                    long newExpire = getExtendExpire(expire);
                    //对数据进行编码操作
                    MapData encodeMap = (MapData) getEncodeData(expire, data, CodecEnum.MAP);
                    //填充缓存
                    transporter.hset(key, newExpire, encodeMap.getData());
                    return data;
                }
            }));
        } else {
            //缓存中存在数据且未过期
            resMap = mapData.getData();
        }

        return resMap;
    }

    @Override
    protected Object getDataIgnoreValid(String key) {
        //检查是否已经有其他线程刷新完缓存
        Map<String, String> cacheMap = this.transporter.hgetAll(key);
        if (MapUtils.isEmpty(cacheMap)) return null;

        MapData mapData = (MapData) this.getDecodeData(cacheMap, CodecEnum.MAP);
        return mapData.getData();
    }

    /**
     * 获取解码后的数据，如果缓存数据已过期，则返回 Constants.EMPTY_MAP
     * @param key key
     * @param field map中的字段的key
     * @return 解码后的数据
     */
    private Map<String, String> getDecodeData(String key, String field) {
        Map<String, String> map = this.transporter.hmget(key, Constants.ACTUAL_EXPIRE_TIME_KEY, field);
        return this.getDecodeData(map);
    }

    /**
     * 获取解码后的数据，如果缓存数据已过期，则返回 Constants.EMPTY_MAP
     * @param map 原始的map数据
     * @return 解码后的数据
     */
    private Map<String, String> getDecodeData(Map<String, String> map) {
        MapData mapData = (MapData) this.getDecodeData(map, CodecEnum.MAP);
        boolean invalid = mapData.isInvalid();

        if (invalid) {
            return Constants.EMPTY_MAP;
        }

        return mapData.getData();
    }
}
