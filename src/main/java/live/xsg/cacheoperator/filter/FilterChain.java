package live.xsg.cacheoperator.filter;

import live.xsg.cacheoperator.extension.ExtensionLoader;
import live.xsg.cacheoperator.support.OrderComparator;

import java.util.LinkedList;
import java.util.List;

/**
 * 过滤器链 builder，单例类
 * Created by xsg on 2020/8/3.
 */
public class FilterChain {
    private static FilterChain holder = new FilterChain();

    private ExtensionLoader<Filter> extensionLoader = new ExtensionLoader<>();
    private List<Filter> filters = new LinkedList<>();

    private FilterChain() {
        this.build();
    }

    public static FilterChain getInstance() {
        return holder;
    }

    /**
     * 创建过滤器链
     */
    public void build() {
        List<Filter> extFilters = this.extensionLoader.getExtensions(Filter.class);
        filters.addAll(extFilters);
    }

    /**
     * 调用所有过滤器链的前置处理
     * @param key key
     * @return true，继续执行后续逻辑；false，不执行后续逻辑
     */
    public boolean preFilter(String key) {
        for (Filter filter : filters) {
            if (!filter.preFilter(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 调用所有过滤器的后置处理
     * @param key key
     * @param result 返回结果
     */
    public void postFilter(String key, Object result) {
        for (Filter filter : filters) {
            try {
                filter.postFilter(key, result);
            } catch (Exception e) {
                //失败不影响后续逻辑
                e.printStackTrace();
            }
        }
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    /**
     * 对filter进行排序
     */
    private void sort() {
        OrderComparator.sort(this.filters);
    }
}
