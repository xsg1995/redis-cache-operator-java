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

    @Override
    public int setIfNotExist(String key, String value, long expire) {
//        Jedis jedis = new Jedis();
//        // 1 if the key was set 0 if the key was not set
//        long res = jedis.setnx(Constants.LOADING_KEY + key, key);
//        if (res == RedisConstants.RESULT_SUCCESS) {
//            //设置过期时间
//            jedis.pexpire(key, expire);
//        }
        return 0;
    }

    @Override
    public void del(String key) {

    }
}
