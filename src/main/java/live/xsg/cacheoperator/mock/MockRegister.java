package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.extension.ExtensionLoader;
import live.xsg.cacheoperator.support.OrderComparator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * mock降级数据注册
 * Created by xsg on 2020/8/6.
 */
public class MockRegister {

    private ExtensionLoader<Mock> extensionLoader = new ExtensionLoader<>();
    //mock实现类
    private List<Mock> mocks = new LinkedList<>();
    //判断 mock 的key是否存在
    private Map<String, Object> mockMap = new ConcurrentHashMap<>();
    //保证添加mock实现的线程安全
    private Lock LOCK = new ReentrantLock();
    //占位对象
    private Object OBJECT = new Object();

    static class MockRegisterHolder {
        private static MockRegister holder = new MockRegister();
    }

    public static MockRegister getInstance() {
        return MockRegisterHolder.holder;
    }

    private MockRegister() {
        List<Mock> extensions = extensionLoader.getExtensions(Mock.class);
        mocks.addAll(extensions);
    }

    /**
     * 注册mock
     * @param mock 实现的mock逻辑
     */
    public void register(Mock mock) {
        LOCK.lock();
        try {
            this.mocks.add(mock);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * 添加 key 对应的 mock 实现，如果已经存在 key 对应的 mock 实现，则不添加
     * @param key key
     * @param mock mock 实现
     */
    public void register(String key, Mock mock) {
        Object obj = this.mockMap.putIfAbsent(key, OBJECT);
        if (obj == null) {
            LOCK.lock();
            try {
                this.mocks.add(new MatchKeyWrapperMock(key, mock));
            } finally {
                LOCK.unlock();
            }
        }
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
