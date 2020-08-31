package live.xsg.cacheoperator.utils;

import java.util.Collection;

/**
 * Collection工具类
 * Created by xsg on 2020/8/31.
 */
public class CollectionUtils {

    /**
     * 判断集合是否为空
     * @param collection 集合
     * @return 为空返回true；否则返回false
     */
    public static boolean isEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }
}
