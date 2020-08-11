package live.xsg.cacheoperator;

import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Created by xsg on 2020/8/11.
 */
public class RedisCacheOperatorTest {

    private Transporter transporter = new RedisTransporter();

    @Test
    public void getString_with_fluster_test() {
        String key = "sayHello";
        String sourceValue = "hello world!";
        long expire = 10 * 60 * 1000;  //10 分钟

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.getString(key, expire, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        });

        transporter.del(key);
        assertEquals(sourceValue, cacheValue);
    }

    @Test
    public void getStringAsync_with_fluster_test() {
        String key = "sayHello";
        String sourceValue = "hello world!";
        String defaultValue = "Hi!";
        long expire = 10 * 60 * 1000;  //10 分钟

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.getStringAsync(key, expire, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        }, defaultValue);

        assertEquals(cacheValue, defaultValue);
        sleep(2);

        cacheValue = cacheOperator.getString(key);
        assertEquals(cacheValue, sourceValue);

        transporter.del(key);
    }

    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
        }
    }
}
