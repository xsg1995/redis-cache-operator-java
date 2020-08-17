package live.xsg.cacheoperator;

import java.util.Map;

/**
 * map类型操作接口
 * Created by xsg on 2020/8/17.
 */
public interface MapOperator {

    /**
     * 从缓存中获取数据，Map类型，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_MAP
     * @param key key
     * @return 返回缓存数据，如果缓存中无数据或者缓存过期，则返回 Constants.EMPTY_STRING
     */
    Map<String, String> getAllMap(String key);
}
