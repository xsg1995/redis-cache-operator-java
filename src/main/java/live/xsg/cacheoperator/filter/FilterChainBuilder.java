package live.xsg.cacheoperator.filter;

import java.util.LinkedList;
import java.util.List;

/**
 * 过滤器链 builder
 * Created by xsg on 2020/8/3.
 */
public class FilterChainBuilder {

    /**
     * 创建过滤器链
     * @return 过滤器链，如果没有过滤器链，返回空集合，不是 null
     */
    public static List<Filter> build() {
        List<Filter> filters = new LinkedList<>();

        return filters;
    }
}
