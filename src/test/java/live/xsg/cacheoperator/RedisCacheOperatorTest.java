package live.xsg.cacheoperator;

import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Created by xsg on 2020/7/31.
 */
@Test
public class RedisCacheOperatorTest {

    public void getString_with_test() {
        CacheOperator cacheOperator = new RedisCacheOperator();
        String key = "key1";
        String value = "value1";
        long expire = 2 * 1000;
        String val = cacheOperator.getString(key, expire, () -> {
            System.out.println("加载数据...");
            return value;
        });
        assertEquals(val, value);
    }

    public void getStringAsync_test() throws InterruptedException {
        CacheOperator cacheOperator = new RedisCacheOperator();
        String key = "key1";
        String value = "value1";
        long expire = 2000;

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getStringAsync(key, expire, () -> {
                    System.out.println("加载数据...");
                    return value;
                });
                System.out.println(res);
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }

        sleep(1);


        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getStringAsync(key, expire, () -> {
                    System.out.println("加载数据...");
                    return value;
                });
                System.out.println(res);
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }

    public void getStringAsync_default_test() throws InterruptedException {
        CacheOperator cacheOperator = new RedisCacheOperator();
        String key = "key1";
        String value = "value1";
        long expire = 2000;

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getStringAsync(key, expire, () -> {
                    System.out.println("加载数据...");
                    return value;
                }, "defaultVal");
                System.out.println(res);
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }

        sleep(1);

        list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getStringAsync(key, expire, () -> {
                    System.out.println("加载数据...");
                    return value;
                }, "defaultVal");
                System.out.println(res);
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }
    }

    public void failback_retry_test() {
        RedisCacheOperator cacheOperator = new RedisCacheOperator();
        String key = "key";
        String value = "value";
        long expire = 10 * 1000L;
        String val = cacheOperator.getString(key, expire, () -> value);
        System.out.println("failback_retry_test_return1:" + val);

        val = cacheOperator.getString(key, expire, () -> value);
        System.out.println("failback_retry_test_return2:" + val);

        sleep(60);

        val = cacheOperator.getString(key, expire, () -> value);
        System.out.println("failback_retry_test_return3:" + val);
    }

    //线程睡眠
    public void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
