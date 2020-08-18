package live.xsg.cacheoperator.utils;

import java.util.Map;

/**
 * map工具类
 * Created by xsg on 2020/8/18.
 */
public class MapUtils {

    /**
     * map为null或者为空，则返回true，否则返回false
     * @param map map
     * @return map为null或者为空，则返回true，否则返回false
     */
    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }
}
