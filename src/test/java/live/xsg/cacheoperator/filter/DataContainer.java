package live.xsg.cacheoperator.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 模拟测试数据
 * Created by xsg on 2020/8/4.
 */
public class DataContainer {

    /**
     * 获取要过滤的key
     */
    public static Map<String, String> getMockData() {

        Map<String, String> mockDataMap = new HashMap<>();

        for (int i = 0; i < 10000; i++) {
            String key = "key_" + i;
            String val = "val" + i;
            mockDataMap.put(key, val);
        }

        return mockDataMap;
    }


}
