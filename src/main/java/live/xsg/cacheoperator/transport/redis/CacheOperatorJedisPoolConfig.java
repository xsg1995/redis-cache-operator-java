package live.xsg.cacheoperator.transport.redis;

import redis.clients.jedis.JedisPoolConfig;

/**
 * redis配置信息
 * Created by xsg on 2020/7/31.
 */
public class CacheOperatorJedisPoolConfig extends JedisPoolConfig {

    public static final String host = "127.0.0.1";
    public static final int port = 6379;
    public static final int timeOut = 2000;

    private static final int MAX_TOTAL = 20;
    private static final int MAX_IDLE = 20;
    private static final int MIN_IDLE = 10;
    private static final long MAX_WAIT_MILLIS = 1000;
    private static final boolean TEST_ON_BORROW = true;

    public CacheOperatorJedisPoolConfig() {
        super();
        setMaxTotal(MAX_TOTAL);
        setMaxIdle(MAX_IDLE);
        setMinIdle(MIN_IDLE);
        setMaxWaitMillis(MAX_WAIT_MILLIS);
        setTestOnBorrow(TEST_ON_BORROW);
    }
}
