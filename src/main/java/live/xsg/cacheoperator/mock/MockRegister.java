package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.CacheOperator;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * mock降级数据注册
 * Created by xsg on 2020/8/6.
 */
public class MockRegister {
    //mock实现类
    private List<CacheOperator> mockCacheOperators = new LinkedList<>();

    static class MockRegisterHolder {
        private static MockRegister holder = new MockRegister();
    }

    public static MockRegister getInstance() {
        return MockRegisterHolder.holder;
    }

    /**
     * 注册mock
     * @param mockCacheOperator 实现的mock逻辑
     */
    public void register(CacheOperator mockCacheOperator) {
        this.mockCacheOperators.add(mockCacheOperator);
    }

    /**
     * 获取mock实现类列表
     * @return mock实现类列表
     */
    public Iterator<CacheOperator> getMockCacheOperators() {
        List<CacheOperator> cacheOperators = Collections.unmodifiableList(this.mockCacheOperators);
        return cacheOperators.iterator();
    }
}
