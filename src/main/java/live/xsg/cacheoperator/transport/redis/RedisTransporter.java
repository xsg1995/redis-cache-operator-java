package live.xsg.cacheoperator.transport.redis;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.transport.Transporter;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

/**
 * 与redis交互底层接口
 * Created by xsg on 2020/7/20.
 */
public class RedisTransporter implements Transporter {

    private JedisPool jedisPool;

    public RedisTransporter() {
        this(new CacheOperatorJedisPoolConfig());
    }

    public RedisTransporter(GenericObjectPoolConfig poolConfig) {
        String host = CacheOperatorJedisPoolConfig.host;
        int port = CacheOperatorJedisPoolConfig.port;
        int timeout = CacheOperatorJedisPoolConfig.timeOut;
        this.jedisPool = new JedisPool(poolConfig, host, port, timeout);
    }

    public RedisTransporter(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    public String getString(String key) {
        return execute((jedis) -> jedis.get(key));
    }

    @Override
    public String set(String key, long expire, String value) {
        return execute(jedis -> jedis.psetex(key, expire, value));
    }

    @Override
    public int setIfNotExist(String key, String value, long expire) {
        return execute((jedis) -> {
            int res = Integer.parseInt(jedis.setnx(key, value).toString());
            if (res == Constants.RESULT_SUCCESS) {
                //设置过期时间
                jedis.pexpire(key, expire);
            }
            return res;
        });
    }

    @Override
    public void del(String key) {
        execute(jedis -> jedis.del(key));
    }

    @Override
    public void incr(String key) {
        this.execute(jedis -> jedis.incr(key));
    }

    @Override
    public Map<String, String> getAllMap(String key) {
        return this.execute(jedis -> jedis.hgetAll(key));
    }

    private <T> T execute(JedisExecutor<T> executor) {
        T res = null;
        try (Jedis jedis = this.jedisPool.getResource()) {
            res = executor.executor(jedis);
        }
        return res;
    }
}
