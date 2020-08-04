package live.xsg.cacheoperator.filter;

import live.xsg.cacheoperator.extension.ExtensionLoader;

import java.util.LinkedList;
import java.util.List;

/**
 * 过滤器链 builder，单例类
 * Created by xsg on 2020/8/3.
 */
public class FilterChainBuilder {

    static class FilterChainBuilderHolder {
        private static FilterChainBuilder holder = new FilterChainBuilder();
    }

    private ExtensionLoader<Filter> extensionLoader = new ExtensionLoader<>();
    private List<Filter> filters = new LinkedList<>();


    private FilterChainBuilder() {}

    public static FilterChainBuilder getInstance() {
        return FilterChainBuilderHolder.holder;
    }

    /**
     * 创建过滤器链
     * @return 过滤器链，如果没有过滤器链，返回空集合，不是 null
     */
    public List<Filter> build() {
        List<Filter> extFilters = this.extensionLoader.getExtensions(Filter.class);
        filters.addAll(extFilters);
        return this.filters;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }
}
