package live.xsg.cacheoperator.transport.redis;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.MapUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
    public String get(String key) {
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
    public void pexpire(String key, long expire) {
        this.execute(jedis -> jedis.pexpire(key, expire));
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return this.execute(jedis -> jedis.hgetAll(key));
    }

    @Override
    public void hset(String key, long expire, Map<String, String> data) {
        if (MapUtils.isEmpty(data)) return;

        data.forEach((k, v) -> this.execute(jedis -> jedis.hset(key, k, v)));
        this.pexpire(key, expire);
    }

    @Override
    public String hget(String key, String field) {
        return this.execute(jedis -> jedis.hget(key, field));
    }

    @Override
    public Map<String, String> hmget(String key, String... fields) {
        return this.execute(jedis -> {
            List<String> hmget = jedis.hmget(key, fields);

            Map<String, String> result = new HashMap<>();
            List<String> fieldsList = (fields != null) ? Arrays.asList(fields) : null;
            if (fieldsList != null && !fieldsList.isEmpty()) {
                for (int i = 0; i < fieldsList.size(); i++) {
                    String field = fieldsList.get(i);
                    String value = hmget.get(i);
                    result.put(field, value);
                }
            }
            return result;
        });
    }

    private <T> T execute(JedisExecutor<T> executor) {
        T res = null;
        try (Jedis jedis = this.jedisPool.getResource()) {
            res = executor.executor(jedis);
        }
        return res;
    }
}
