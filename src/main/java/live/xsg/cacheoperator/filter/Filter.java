package live.xsg.cacheoperator.filter;

/**
 * 过滤器
 * Created by xsg on 2020/8/3.
 */
public interface Filter {

    /**
     * 前置过滤器，如果返回false，则不执行后续逻辑，返回true，则可以执行后续逻辑
     * @param key key
     * @return false or true
     */
    boolean preFilter(String key);

    /**
     * 后置过滤器
     * @param key key
     * @param result 操作结果
     */
    void postFilter(String key, Object result);
}
