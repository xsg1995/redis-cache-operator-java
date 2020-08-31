package live.xsg.cacheoperator;

import live.xsg.cacheoperator.core.ListOperator;
import live.xsg.cacheoperator.core.MapOperator;
import live.xsg.cacheoperator.core.StringOperator;

/**
 * 缓存操作器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator extends StringOperator, MapOperator, ListOperator {

    /**
     * 删除key
     * @param key key
     */
    void del(String key);

}
