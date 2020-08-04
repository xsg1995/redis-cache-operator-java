package live.xsg.cacheoperator;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.filter.Filter;
import live.xsg.cacheoperator.filter.FilterChainBuilder;
import live.xsg.cacheoperator.transport.MapTransporter;
import live.xsg.cacheoperator.transport.Transporter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.testng.Assert.assertEquals;

/**
 * Created by xsg on 2020/7/20.
 */
@Test
public class RedisCacheOperatorWithMapTest {

    public void getString_with_map_test() throws InterruptedException {
        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
        String key = "key1";
        String value = "value1";
        long expire = 2000;
        String val = cacheOperator.getString(key, expire, () -> {
            System.out.println("加载数据...");
            return value;
        });
        assertEquals(val, value);

        sleep(2);

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getString(key, expire, () -> {
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

    public void getStringAsync_with_map_test() throws InterruptedException {
        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
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

    public void getStringAsync_with_executor_map_test() throws InterruptedException {
        Executor executor = new ThreadPoolExecutor(3, 3,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1),
                new ThreadPoolExecutor.DiscardPolicy() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
                        System.out.println("reject");
                    }
                });
        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
        String key = "key1";
        String value = "value1";
        long expire = 2000;

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.getStringAsync(key, expire, () -> {
                    System.out.println("加载数据...");
                    return value;
                }, executor);
            }));
        }

        for (Thread thread : list) {
            thread.start();
        }
        for (Thread thread : list) {
            thread.join();
        }

    }

    public void getString_with_test() {
        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
        String key = "key";
        String value = cacheOperator.getString(key);
        assertEquals(value, Constants.EMPTY_STRING);

        String val = "val";
        transporter.set(key, 0L, val);
        value = cacheOperator.getString(key);
        assertEquals(value, val);
    }

    public void filter_test() {
        FilterChainBuilder instance = FilterChainBuilder.getInstance();
        instance.addFilter(new Filter() {
            @Override
            public boolean preFilter(String key) {
                return "can_access".equals(key);
            }

            @Override
            public void postFilter(String key, Object result) {
                System.out.println("自定义filter:" + key + "---->" + result);
            }
        });

        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
        String key = "key";
        String canAccess = "can_access";

        System.out.println(cacheOperator.getString(key));
        System.out.println(cacheOperator.getString(canAccess));
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
