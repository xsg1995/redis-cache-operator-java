package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;

/**
 * string类型操作实现
 * Created by xsg on 2020/8/17.
 */
public class RedisStringOperator extends AbstractRedisOperator implements StringOperator {

    //异步任务执行器
    protected CacheExecutor<String> asyncCacheExecutor = new AsyncCacheExecutor();

    public RedisStringOperator(Transporter transporter, CacheOperator cacheOperator, ResourceLoader resourceLoader) {
        super(transporter, cacheOperator, resourceLoader);
    }

    @Override
    public String getString(String key) {
        String res = this.transporter.getString(key);
        StringCodec.StringData stringData = (StringCodec.StringData) this.getDecodeData(res, CodecEnum.STRING);
        boolean invalid = this.isInvalid(stringData.getActualExpireTime());

        if (invalid) {
            //缓存数据过期
            return Constants.EMPTY_STRING;
        }
        return stringData.getData();
    }

    @Override
    public String getString(String key, long expire, Refresher<String> flusher) {
        return this.getString(key, expire, flusher, new SyncCacheExecutor<>());
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher) {
        return this.getString(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService) {
        return this.getString(key, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    private String getString(String key, long expire, Refresher<String> flusher, CacheExecutor<String> cacheExecutor) {
        String res = this.transporter.getString(key);

        //数据解码
        StringCodec.StringData stringData = (StringCodec.StringData) this.getDecodeData(res, CodecEnum.STRING);
        boolean invalid = this.isInvalid(stringData.getActualExpireTime());

        if (invalid) {
            //缓存过期获取缓存中无数据，刷新缓存
            res = cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
        } else {
            //缓存中存在数据且未过期
            res = stringData.getData();
        }

        if (res == null) {
            res = Constants.EMPTY_STRING;
        }
        return res;
    }

    /**
     * 填充数据到缓存中
     * @param key key
     * @param expire 缓存过期时间
     * @param flusher 获取缓存数据
     * @return 返回最新数据
     */
    protected String doFillStringCache(String key, long expire, Refresher<String> flusher) {
        boolean isLoading = false;
        try {
            //设置正在加载key对应的数据
            isLoading = this.isLoading(key);
            //isLoading=true，则已有其他线程在刷新数据
            if (isLoading) {
                return (String) this.blockIfNeed(key);
            }

            String data = flusher.refresh();

            long newExpire = this.getExtendExpire(expire);
            String encode = (String) this.getEncodeData(expire, data, CodecEnum.STRING);
            this.transporter.set(key, newExpire, encode);

            return data;
        } finally {
            if (!isLoading) {
                //设置key已经加载完毕
                this.loadFinish(key);
            }
        }
    }

    @Override
    protected String getDataIgnoreValid(String key) {
        String res = this.transporter.getString(key);
        if (StringUtils.isBlank(res)) return null;

        StringCodec.StringData stringData = (StringCodec.StringData) this.getDecodeData(res, CodecEnum.STRING);
        return stringData.getData();
    }


}
