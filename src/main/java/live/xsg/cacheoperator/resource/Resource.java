package live.xsg.cacheoperator.resource;

/**
 * 获取资源中的数据
 * Created by xsg on 2020/8/7.
 */
public interface Resource {

    /**
     * 获取字符串类型数据
     * @param key key
     * @return 返回key对应的value，如果不存在，则返回null
     */
    String getString(String key, String defaultVal);

    /**
     * 获取int类型的数据
     * @param key key
     * @return 返回key对应的value，如果不存在，返回null
     */
    Integer getInt(String key, int defaultVal);

    /**
     * 获取long类型的数据
     * @param key key
     * @param defaultVal 默认值
     * @return 返回key对应的value，如果不存在，返回null
     */
    Long getLong(String key, long defaultVal);

    /**
     * 设置属性key和value
     * @param key key
     * @param value value
     */
    void set(String key, String value);
}
