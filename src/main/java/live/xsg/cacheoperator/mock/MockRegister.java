package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.extension.ExtensionLoader;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * mock降级数据注册
 * Created by xsg on 2020/8/6.
 */
public class MockRegister {

    private ExtensionLoader<Mock> extensionLoader = new ExtensionLoader<>();
    //mock实现类
    private List<CacheOperator> mockCacheOperators = new LinkedList<>();

    static class MockRegisterHolder {
        private static MockRegister holder = new MockRegister();
    }

    public static MockRegister getInstance() {
        return MockRegisterHolder.holder;
    }

    private MockRegister() {
        List<Mock> extensions = extensionLoader.getExtensions(Mock.class);
        for (Mock mock : extensions) {
            if (mock instanceof CacheOperator) {
                mockCacheOperators.add((CacheOperator) mock);
            }
        }
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
