package live.xsg.cacheoperator;

/**
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheFlusherTest {

    public void testSetString() {
        long expire = 1000;
        CacheOperator cacheOperator = new RedisCacheOperator();

        String value = cacheOperator.loadString("key", expire, () -> "value");
        System.out.println(value);
    }
}
