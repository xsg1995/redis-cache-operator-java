package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;
import org.apache.commons.lang3.StringUtils;

/**
 * redis缓存操作器
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheOperator extends AbstractCacheOperator implements CacheOperator {

    public RedisCacheOperator() {
        super(new RedisTransporter(), DEFAULT_LOADING_KEY_EXPIRE);
    }

    public RedisCacheOperator(long loadingKeyExpire) {
        super(new RedisTransporter(), loadingKeyExpire);
    }

    public RedisCacheOperator(Transporter transporter) {
        super(transporter, DEFAULT_LOADING_KEY_EXPIRE);
    }

    public RedisCacheOperator(Transporter transporter, long loadingKeyExpire) {
        super(transporter, loadingKeyExpire);
    }

    @Override
    public String loadString(String key, long expire, Refresher<String> flusher) {
        String res = this.transporter.get(key);

        if (StringUtils.isBlank(res)) {
            //缓存中不存在数据，获取数据，放入缓存
            res = this.doFillStringCache(key, expire, flusher);
        } else {
            //缓存中存在数据，判断缓存是否已经过期
            StringCodec.StringData stringData = this.getDecodeStringData(res);
            boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

            if (invalid) {
                //缓存过期，刷新缓存
                res = this.doFillStringCache(key, expire, flusher);
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

    @Override
    public String getString(String key) {
        String res = this.transporter.get(key);
        StringCodec.StringData stringData = this.getDecodeStringData(res);
        boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

        if (invalid) {
            //缓存数据过期
            return Constants.EMPTY_STRING;
        }
        return stringData.getData();
    }

}
