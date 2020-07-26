package live.xsg.cacheoperator;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.transport.MapTransporter;
import live.xsg.cacheoperator.transport.Transporter;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Created by xsg on 2020/7/20.
 */
@Test
public class RedisCacheFlusherTest {

    public void loadString_with_map_test() throws InterruptedException {
        Transporter transporter = new MapTransporter();
        CacheOperator cacheOperator = new RedisCacheOperator(transporter);
        String key = "key1";
        String value = "value1";
        long expire = 2000;
        String val = cacheOperator.loadString(key, expire, () -> {
            System.out.println("加载数据...");
            return value;
        });
        assertEquals(val, value);

        sleep(2);

        List<Thread> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(new Thread(() -> {
                String res = cacheOperator.loadString(key, expire, () -> {
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

        sleep(2);
        String res = cacheOperator.loadString(key, expire, () -> {
            System.out.println("加载数据...");
            return value;
        });

        assertEquals(val, value);
    }

    public void getString_test() {
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

    //线程睡眠
    public void sleep(int second) {
        try {
            TimeUnit.SECONDS.sleep(second);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
