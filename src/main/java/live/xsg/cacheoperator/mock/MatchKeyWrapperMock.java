package live.xsg.cacheoperator.mock;

import java.lang.reflect.Method;

/**
 * 当外部传入 key 与查询的 key 相同时，直接执行 mock 逻辑
 * Created by xsg on 2020/9/25.
 */
public class MatchKeyWrapperMock implements Mock {

    //目标 key
    private String targetKey;
    //mock 实现
    private Mock mock;

    public MatchKeyWrapperMock(String targetKey, Mock mock) {
        this.targetKey = targetKey;
        this.mock = mock;
    }

    @Override
    public Object mock(String key, Method method) {
        if (key == null) throw new IllegalArgumentException("查询的 key 不能为空");

        if (targetKey == null) return mock.mock(key, method);

        if (targetKey.equals(key)) return mock.mock(key, method);

        return null;
    }
}
