package live.xsg.cacheoperator.transport.redis;

import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.utils.MapUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * 与redis交互底层接口
 * Created by xsg on 2020/7/20.
 */
public class JedisTransporter implements Transporter {

    private JedisPool jedisPool;

    public JedisTransporter() {
        this(new GenericObjectPoolConfig());
    }

    public JedisTransporter(GenericObjectPoolConfig poolConfig) {
        String host = CacheOperatorJedisPoolConfig.host;
        int port = CacheOperatorJedisPoolConfig.port;
        int timeout = CacheOperatorJedisPoolConfig.timeOut;
        this.jedisPool = new JedisPool(poolConfig, host, port, timeout);
    }

    public JedisTransporter(JedisPool jedisPool) {
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
    public boolean setIfNotExist(String key, String value, long expire) {
        String script =
                "local key = KEYS[1] " +
                        "local value = ARGV[1] " +
                        "local expire = ARGV[2] " +
                        "if redis.call('exists', key) == 1 then " +
                        "   return 0 " +
                        "else " +
                        "   redis.call('set', key, value)" +
                        "   redis.call('pexpire', key, expire) " +
                        "end " +
                        "return 1";
        return this.execute(jedis -> {
            long ret = (long) jedis.eval(script, Collections.singletonList(key),
                    Arrays.asList(value, String.valueOf(expire)));
            return ret == 1L;
        });
    }

    @Override
    public void del(String key) {
        execute(jedis -> jedis.del(key));
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

    @Override
    public List<String> lrange(String key, long start, long end) {
        return this.execute(jedis -> jedis.lrange(key, start, end));
    }

    @Override
    public Long lpush(String key, long expire, String... strings) {
        Long res = this.execute(jedis -> jedis.lpush(key, strings));
        this.pexpire(key, expire);
        return res;
    }

    @Override
    public boolean exists(String key) {
        return this.execute(jedis -> jedis.exists(key));
    }

    @Override
    public Set<String> smembers(String key) {
        return this.execute(jedis -> jedis.smembers(key));
    }

    @Override
    public Long sadd(String key, long expire, String... member) {
        Long result = this.execute(jedis -> jedis.sadd(key, member));
        this.pexpire(key, expire);
        return result;
    }

    private <T> T execute(JedisExecutor<T> executor) {
        T res = null;
        try (Jedis jedis = this.jedisPool.getResource()) {
            res = executor.executor(jedis);
        }
        return res;
    }
}
