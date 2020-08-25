package live.xsg.cacheoperator.transport;

import java.util.Map;

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
    String set(String key, long expire, String value);

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

    /**
     * 累加
     * @param key key
     */
    void incr(String key);

    /**
     * 设置key的过期时间
     * @param key key
     * @param expire 过期时间
     */
    void pexpire(String key, long expire);

    /**
     * 根据 key 获取 map 类型的值
     * @param key key
     * @return value
     */
    Map<String, String> hgetAll(String key);

    /**
     * 设置map
     * @param key key
     * @param expire 过期时间，单位: ms
     * @param data data
     */
    void hset(String key, long expire, Map<String, String> data);

    /**
     * 获取map中的某个字段的值
     * @param key map对应的key
     * @param field map中某个字段的key
     * @return map中对应字段的值
     */
    String hget(String key, String field);
}
