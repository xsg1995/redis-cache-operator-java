package live.xsg.cacheoperator.mock;

import live.xsg.cacheoperator.CacheOperator;

import java.lang.reflect.Method;

/**
 * mock接口
 * Created by xsg on 2020/8/7.
 */
public interface Mock {

    /**
     * 执行mock降级，根据key查找对应的策略
     * @param key key
     * @param cacheOperator 操作的cacheOperator对象
     * @param method 调用的方法
     * @return 结果
     */
    Object mock(String key, CacheOperator cacheOperator, Method method);
}
