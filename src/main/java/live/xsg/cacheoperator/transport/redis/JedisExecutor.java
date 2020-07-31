package live.xsg.cacheoperator.transport.redis;

import redis.clients.jedis.Jedis;

/**
 * jedis执行任务接口
 * Created by xsg on 2020/7/31.
 */
public interface JedisExecutor<T> {

    T executor(Jedis jedis);
}
