package live.xsg.cacheoperator.transport;

/**
 * 与缓存服务器交互接口
 * Created by xsg on 2020/7/20.
 */
public interface Transporter {

    /**
     * 根据 key 获取 value
     * @param key key
     * @return value
     */
    String get(String key);

    /**
     * 设置key value
     * @param key key
     * @param expire 获取时间
     * @param value value
     */
    void set(String key, long expire, String value);
}
