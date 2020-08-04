package live.xsg.cacheoperator;

import live.xsg.cacheoperator.filter.DataContainer;
import org.apache.commons.lang3.StringUtils;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by xsg on 2020/8/4.
 */
@Test
public class FilterTest {

    public void bloom_filter_test() throws Exception {
        //live.xsg.cacheoperator.filter.CacheBloomFilter

        Map<String, String> mockData = DataContainer.getMockData();
        int size = mockData.size();

        RedisCacheOperator cacheOperator = new RedisCacheOperator();

        int threadNum = 10;
        CompletionService<Void> cs = new ExecutorCompletionService<>(Executors.newFixedThreadPool(threadNum));

        for (int j = 0; j < threadNum; j++) {
            cs.submit(() -> {
                for (int i = 0; i < size; i++) {
                    String key = "key_" + i;
                    String value = cacheOperator.getString(key, 2 * 1000, () -> mockData.get(key));
                    if (StringUtils.isNotBlank(value)) {
                        System.out.println(key + "----->" + value);
                    }
                }
                return null;
            });
        }

        for (int i = 0; i < threadNum; i++) {
            cs.take().get();
        }
    }
}
