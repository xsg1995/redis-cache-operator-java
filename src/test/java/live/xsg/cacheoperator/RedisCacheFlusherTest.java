package live.xsg.cacheoperator;

/**
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheFlusherTest {

    public void testSetString() {
        long expire = 1000;
        CacheOperator flusher = new RedisCacheOperator();

        flusher.loadString("key", expire, () -> "value");
    }
}
