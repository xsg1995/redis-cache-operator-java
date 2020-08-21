package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.extension.ExtensionLoader;
import live.xsg.cacheoperator.support.OrderComparator;

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
    private List<Mock> mocks = new LinkedList<>();

    static class MockRegisterHolder {
        private static MockRegister holder = new MockRegister();
    }

    public static MockRegister getInstance() {
        return MockRegisterHolder.holder;
    }

    private MockRegister() {
        List<Mock> extensions = extensionLoader.getExtensions(Mock.class);
        for (Mock mock : extensions) {
            mocks.add(mock);
        }
    }

    /**
     * 注册mock
     * @param mock 实现的mock逻辑
     */
    public void register(Mock mock) {
        this.mocks.add(mock);
    }

    /**
     * 获取mock实现类列表
     * @return mock实现类列表
     */
    public Iterator<Mock> getMockCacheOperators() {
        List<Mock> cacheOperators = Collections.unmodifiableList(this.mocks);
        return cacheOperators.iterator();
    }

    /**
     * 对Mock进行排序
     */
    private void sort() {
        OrderComparator.sort(this.mocks);
    }
}
