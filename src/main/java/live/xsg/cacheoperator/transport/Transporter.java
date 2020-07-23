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
     * @param expire 过期时间，单位: ms
     * @param value value
     */
    void set(String key, long expire, String value);

    /**
     * 设置key对应的value，如果key不存在则设置，否则不设置
     * @param key key
     * @param value value
     * @param expire 过期时间,单位:ms
     * @return 设置成功，返回 1，否则返回 2
     */
    int setIfNotExist(String key, String value, long expire);

    /**
     * 删除key值
     * @param key key
     */
    void del(String key);
}
