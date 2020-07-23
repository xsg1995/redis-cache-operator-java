package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.Codec;
import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.common.RedisConstants;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;
import org.apache.commons.lang3.StringUtils;

/**
 * redis缓存操作器
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheOperator implements CacheOperator {

    //默认刷新缓存的最大时间，2分钟，单位：ms
    private static final long DEFAULT_LOADING_KEY_EXPIRE = 2 * 2 * 1000L;

    //刷新缓存的最大时间
    private long loadingKeyExpire;
    //服务器交互接口 RedisTransporter
    private Transporter transporter;
    //String类型编解码
    private Codec stringCodec = new StringCodec();

    public RedisCacheOperator() {
        this(DEFAULT_LOADING_KEY_EXPIRE);
    }

    public RedisCacheOperator(long loadingKeyExpire) {
        this(new RedisTransporter(), loadingKeyExpire);
    }

    public RedisCacheOperator(Transporter transporter) {
        this(transporter, DEFAULT_LOADING_KEY_EXPIRE);
    }

    public RedisCacheOperator(Transporter transporter, long loadingKeyExpire) {
        this.transporter = transporter;
    }

    @Override
    public String loadString(String key, long expire, Refresher<String> flusher) {
        String res = this.transporter.get(key);

        if (StringUtils.isBlank(res)) {
            //缓存中不存在数据，获取数据，放入缓存
            res = this.fillString(key, expire, flusher, true);
        } else {
            //缓存中存在数据，判断缓存是否已经过期
            StringCodec.StringData stringData = (StringCodec.StringData) this.stringCodec.decode(res);
            boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

            if (invalid) {
                //缓存过期，刷新缓存
                res = this.fillString(key, expire, flusher, true);
            } else {
                //未过期
                res = stringData.getData();
            }
        }

        return res;
    }

    @Override
    public String getString(String key) {
        String res = this.transporter.get(key);
        StringCodec.StringData stringData = (StringCodec.StringData) this.stringCodec.decode(res);
        return stringData.getData();
    }

    /**
     * 判断缓存的绝对过期时间是否过期
     * @param absoluteExpireTime 缓存绝对过期时间，单位：ms
     * @return 返回true，则过期，返回false，则未过期
     */
    private boolean isInvalid(long absoluteExpireTime) {
        return absoluteExpireTime > System.currentTimeMillis();
    }

    /**
     * 填充数据到缓存中
     * @param key key
     * @param expire 缓存过期时间
     * @param flusher 获取缓存数据
     * @param checkLoading 是否控制同时只能有一个线程刷新缓存，true为是；false为否；
     * @return 返回最新数据
     */
    private String fillString(String key, long expire, Refresher<String> flusher, boolean checkLoading) {
        boolean loading = false;
        try {
            //设置正在加载key对应的数据
            loading = checkLoading && this.loading(key);

            //loading=true，则已有其他线程在刷新数据
            if (loading) {
                return Constants.EMPTY_STRING;
            }

            String data = flusher.refresh();
            if (StringUtils.isBlank(data)) {
                data = Constants.EMPTY_STRING;
            }
            this.transporter.set(key, expire, (String) this.stringCodec.encode(expire, data));
            return data;
        } finally {
            if (checkLoading && !loading) {
                //设置key已经加载完毕
                this.loadFinish(key);
            }
        }
    }

    /**
     * 设置key对应的数据已经加载完毕
     * @param key key
     */
    private void loadFinish(String key) {
        this.transporter.del(key);
    }

    /**
     * 设置key对应的数据正在加载，如果没有其他线程在刷新数据，则当前线程进行刷新
     * @param key key
     * @return 返回true，则说明已有其他线程正在刷新，返回false，则表示没有其他线程在刷新
     */
    private boolean loading(String key) {
        int res = this.transporter.setIfNotExist(key, key, this.loadingKeyExpire);

        return res == RedisConstants.RESULT_FAILURE;
    }

}
