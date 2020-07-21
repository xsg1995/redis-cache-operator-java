package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.Codec;
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
public class RedisCacheOperator implements CacheOperator {

    //服务器交互接口 RedisTransporter
    private Transporter transporter;
    //String类型编解码
    private Codec stringCodec = new StringCodec();

    public RedisCacheOperator() {
        this.transporter = new RedisTransporter();
    }

    public RedisCacheOperator(Transporter transporter) {
        this.transporter = transporter;
    }

    @Override
    public String loadString(String key, long expire, Refresher<String> flusher) {
        String res = this.transporter.get(key);

        if (StringUtils.isBlank(res)) {
            //缓存中不存在数据，获取数据，放入缓存
            res = this.fillString(key, expire, flusher);
        } else {
            //缓存中存在数据，判断缓存是否已经过期
            StringCodec.StringData stringData = (StringCodec.StringData) this.stringCodec.decode(res);
            boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

            if (invalid) {
                //缓存过期，刷新缓存
                res = this.fillString(key, expire, flusher);
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
     * @return 返回最新数据
     */
    private String fillString(String key, long expire, Refresher<String> flusher) {
        String data = flusher.refresh();
        if (StringUtils.isBlank(data)) {
            data = Constants.EMPTY_STRING;
        }
        this.transporter.set(key, expire, (String) this.stringCodec.encode(0L, data));
        return data;
    }
}
