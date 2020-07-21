package live.xsg.cacheoperator.transport.redis;

import live.xsg.cacheoperator.transport.Transporter;

/**
 * 与redis交互底层接口
 * Created by xsg on 2020/7/20.
 */
public class RedisTransporter implements Transporter {
    @Override
    public String get(String key) {
        return null;
    }

    @Override
    public void set(String key, long expire, String value) {

    }
}
