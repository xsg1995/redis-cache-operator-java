package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.transport.Transporter;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executor;

/**
 * string类型操作实现
 * Created by xsg on 2020/8/17.
 */
public class RedisStringOperator implements StringOperator {

    //服务器交互接口 RedisTransporter
    private Transporter transporter;
    //缓存操作
    private CacheOperator cacheOperator;
    //异步任务执行器
    protected CacheExecutor<String> asyncCacheExecutor = new AsyncCacheExecutor<>();

    public RedisStringOperator(Transporter transporter, CacheOperator cacheOperator) {
        this.transporter = transporter;
        this.cacheOperator = cacheOperator;
    }

    @Override
    public String getString(String key) {
        String res = this.transporter.getString(key);
        StringCodec.StringData stringData = (StringCodec.StringData) this.cacheOperator.getDecodeData(res, CodecEnum.STRING);
        boolean invalid = this.cacheOperator.isInvalid(stringData.getAbsoluteExpireTime());

        if (invalid) {
            //缓存数据过期
            return Constants.EMPTY_STRING;
        }
        return stringData.getData();
    }

    @Override
    public String getString(String key, long expire, Refresher<String> flusher) {
        return this.getString(key, expire, flusher, new SyncCacheExecutor<String>());
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher) {
        return this.getString(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor) {
        return this.getString(key, expire, flusher, new AsyncCacheExecutor<>(executor));
    }

    private String getString(String key, long expire, Refresher<String> flusher, CacheExecutor<String> cacheExecutor) {
        String res = this.transporter.getString(key);

        if (StringUtils.isBlank(res)) {
            //缓存中不存在数据，获取数据，放入缓存
            res = cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
        } else {
            //缓存中存在数据，判断缓存是否已经过期
            StringCodec.StringData stringData = (StringCodec.StringData) this.cacheOperator.getDecodeData(res, CodecEnum.STRING);
            boolean invalid = this.cacheOperator.isInvalid(stringData.getAbsoluteExpireTime());

            if (invalid) {
                //缓存过期，刷新缓存
                res = cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
                //如果有其他线程在刷新缓存，则返回现在缓存中的值
                if (Constants.EMPTY_STRING.equals(res)) {
                    res = stringData.getData();
                }
            } else {
                //未过期
                res = stringData.getData();
            }
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
            isLoading = this.cacheOperator.isLoading(key);
            //isLoading=true，则已有其他线程在刷新数据
            if (isLoading) {
                return Constants.EMPTY_STRING;
            }

            //检查是否已经有其他线程刷新完缓存
            String res = this.transporter.getString(key);
            if (StringUtils.isNotBlank(res)) {
                StringCodec.StringData stringData = (StringCodec.StringData) this.cacheOperator.getDecodeData(res, CodecEnum.STRING);
                boolean invalid = this.cacheOperator.isInvalid(stringData.getAbsoluteExpireTime());
                if (!invalid) {
                    //没有过期，已有其他线程刷新了缓存，返回缓存数据
                    return stringData.getData();
                }
            }

            String data = flusher.refresh();

            long newExpire = this.cacheOperator.getExtendExpire(expire);
            String encode = (String) this.cacheOperator.getEncodeData(expire, data, CodecEnum.STRING);
            this.transporter.set(key, newExpire, encode);

            if (StringUtils.isBlank(data)) {
                data = Constants.EMPTY_STRING;
            }

            return data;
        } finally {
            if (!isLoading) {
                //设置key已经加载完毕
                this.cacheOperator.loadFinish(key);
            }
        }
    }
}
