package live.xsg.cacheoperator;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.context.RedisCacheContext;
import live.xsg.cacheoperator.filter.Filter;
import live.xsg.cacheoperator.filter.FilterChain;
import live.xsg.cacheoperator.mock.MockRegister;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.*;

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
        String cacheValue = cacheOperator.get(key, expire, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        });

        cacheOperator.del(key);
        assertEquals(sourceValue, cacheValue);
    }

    @Test
    public void getStringAsync_with_fluster_test() throws ExecutionException, InterruptedException {
        String key = "sayHello";
        String sourceValue = "hello world!";
        long expire = 10 * 60 * 1000;  //10 分钟

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.getAsync(key, expire, () -> {
            //执行业务逻辑，获取值
            return sourceValue;
        });

        assertEquals(cacheValue, Constants.EMPTY_STRING);

        Future<String> resultFuture = RedisCacheContext.getContext().getFuture();
        String result = resultFuture.get();
        assertEquals(result, sourceValue);

        cacheOperator.del(key);
    }

    @Test
    public void getString_with_mock_test() {
        String key = "sayHello";
        String sourceValue = "hello world!";
        String mockValue = "i am mock value.";

        MockRegister.getInstance().register((k, method) -> {
            if (key.equals(k)) {
                return mockValue;
            }
            return null;
        });

        CacheOperator cacheOperator = new RedisCacheOperator();
        String cacheValue = cacheOperator.get(key, EXPIRE, () -> {
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

        String ignoreValue = cacheOperator.get(ignoreKey, EXPIRE, () -> {
            //执行业务逻辑，获取值
            return "value";
        });


        String targetKey = "targetKey";
        String targetValue = cacheOperator.get(targetKey, EXPIRE, () -> {
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
        Map<String, String> resMap = cacheOperator.hgetAll(mapKey);
        assertEquals(resMap, Constants.EMPTY_MAP);
    }

    @Test
    public void getAllMap_with_fluster_test() {
        String mapKey = "mapKey";
        Map<String, String> mockData = new HashMap<>();
        mockData.put("value", "mapValue");

        CacheOperator cacheOperator = new RedisCacheOperator();
        Map<String, String> res = cacheOperator.hgetAll(mapKey, EXPIRE, () -> mockData);

        assertEquals(res, mockData);

        cacheOperator.del(mapKey);
    }

    @Test
    public void getAllMapAsync_with_fluster_test() throws ExecutionException, InterruptedException {
        String mapKey = "mapKey";
        Map<String, String> mockData = new HashMap<>();
        mockData.put("value", "mapValue");

        CacheOperator cacheOperator = new RedisCacheOperator();
        Map<String, String> res = cacheOperator.hgetAllAsync(mapKey, EXPIRE, () -> mockData);

        assertEquals(res, Constants.EMPTY_MAP);

        Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture();
        res = future.get();
        assertEquals(res, mockData);

        cacheOperator.del(mapKey);
    }

    @Test
    public void getString_with_block_test() throws InterruptedException {
        String key = "hello";
        CacheOperator cacheOperator = new RedisCacheOperator();

        int nThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(nThread);
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        for (int i = 0; i < nThread; i++) {
            executorService.submit(() -> {
                try {
                    String value = cacheOperator.get(key, EXPIRE, () -> {
                        sleep(10);
                        return "redis-cache-operator-java";
                    });
                    System.out.println("result:" + value);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        cacheOperator.del(key);
    }

    @Test
    public void hget_test() {
        String key = "hgetKey";
        String field = "hgetField";
        CacheOperator cacheOperator = new RedisCacheOperator();
        String result = cacheOperator.hget(key, field);

        assertEquals(result, "");
    }

    @Test
    public void hgetAsync_test() throws ExecutionException, InterruptedException {
        String mapKey = "mapKey";
        Map<String, String> mockData = new HashMap<>();
        mockData.put("value", "mapValue");

        CacheOperator cacheOperator = new RedisCacheOperator();
        String value = cacheOperator.hgetAsync(mapKey, "value", EXPIRE, () -> mockData);
        assertEquals(value, Constants.EMPTY_STRING);

        Future<Map<String, String>> future = RedisCacheContext.getContext().getFuture();
        Map<String, String> res = future.get();
        assertEquals(res, mockData);

        cacheOperator.del(mapKey);
    }

    @Test
    public void lrange_test() throws InterruptedException {
        String key = "fruit";
        List<String> fruits = Arrays.asList("apple", "peach", "lemon", "pear");
        List<String> mockFruits = Arrays.asList("peach", "lemon");

        MockRegister.getInstance().register((k, method) -> {
            if (key.equals(k)) {
                return mockFruits;
            }
            return null;
        });

        CacheOperator cacheOperator = new RedisCacheOperator();

        int nThread = 10;
        CountDownLatch countDownLatch = new CountDownLatch(nThread);
        ExecutorService executorService = Executors.newFixedThreadPool(nThread);
        for (int i = 0; i < nThread; i++) {
            executorService.submit(() -> {
                try {
                    List<String> result = cacheOperator.lrange(key, 0, -1, EXPIRE, () -> {
                        System.out.println("access...........................");
                        sleep(1);
                        return fruits;
                    });
                    System.out.println(result);
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        executorService.shutdown();
        cacheOperator.del(key);
    }

    private void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
        }
    }
}
