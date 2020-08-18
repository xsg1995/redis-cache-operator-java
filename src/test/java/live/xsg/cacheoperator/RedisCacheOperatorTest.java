package live.xsg.cacheoperator;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.filter.Filter;
import live.xsg.cacheoperator.filter.FilterChain;
import live.xsg.cacheoperator.mock.MockRegister;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * Created by xsg on 2020/8/11.
 */
public class RedisCacheOperatorTest {

    public static long EXPIRE = 10 * 60 * 1000;  //10 分钟

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

        cacheOperator.del(key);
        assertEquals(sourceValue, cacheValue);
    }

    @Test
    public void getStringAsync_with_fluster_test() {
        String key = "sayHello";
        String sourceValue = "hello world!";
        long expire = 10 * 60 * 1000;  //10 分钟

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.getStringAsync(key, expire, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        });

        assertEquals(cacheValue, Constants.EMPTY_STRING);
        sleep(2);

        cacheValue = cacheOperator.getString(key);
        assertEquals(cacheValue, sourceValue);

        cacheOperator.del(key);
    }

    @Test
    public void getString_with_mock_test() {
        String key = "sayHello";
        String sourceValue = "hello world!";
        String mockValue = "i am mock value.";

        MockRegister.getInstance().register((k, cacheOperator, method) -> {
            if (key.equals(k)) {
                return mockValue;
            }
            return null;
        });

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.getString(key, EXPIRE, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        });

        assertEquals(cacheValue, mockValue);
    }

    @Test
    public void getString_with_filter_test() {
        String ignoreKey = "ignoreKey";
        FilterChain.getInstance().addFilter(new Filter() {
            @Override
            public boolean preFilter(String key) {
                if (ignoreKey.equals(key)) {
                    return false;
                }
                return true;
            }

            @Override
            public void postFilter(String key, Object result) {
                System.out.println("key:" + key + " 查询结果:" + result);
            }
        });


        CacheOperator cacheOperator = new RedisCacheOperator();

        String ignoreValue = cacheOperator.getString(ignoreKey, EXPIRE, () -> {
            //执行业务逻辑，获取值
            return "value";
        });


        String targetKey = "targetKey";
        String targetValue = cacheOperator.getString(targetKey, EXPIRE, () -> {
            //执行业务逻辑，获取值
            return "value";
        });

        assertNull(ignoreValue);
        assertEquals(targetValue, "value");
    }

    @Test
    public void getAllMap_test() {
        String mapKey = "mapKey";
        CacheOperator cacheOperator = new RedisCacheOperator();
        Map<String, String> resMap = cacheOperator.getAllMap(mapKey);
        assertEquals(resMap, Constants.EMPTY_MAP);
    }

    @Test
    public void getAllMap_with_fluster_test() {
        String mapKey = "mapKey";
        Map<String, String> mockData = new HashMap<>();
        mockData.put("value", "mapValue");

        CacheOperator cacheOperator = new RedisCacheOperator();
        Map<String, String> res = cacheOperator.getAllMap(mapKey, EXPIRE, () -> mockData);

        assertEquals(res, mockData);

        cacheOperator.del(mapKey);
    }

    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
        }
    }
}
