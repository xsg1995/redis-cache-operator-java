package live.xsg.cacheoperator.transport;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * @return 设置成功，返回 true，否则返回 false
     */
    boolean setIfNotExist(String key, String value, long expire);

    /**
     * 删除key值
     * @param key key
     */
    void del(String key);

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
     * 获取多个map中的字段
     * @param key map对应的key
     * @param fields map中要获取字段的key
     * @return map中的数据
     */
    Map<String, String> hmget(String key, String... fields);

    /**
     * 获取list数据
     * @param key key
     * @param start start
     * @param end end
     * @return 返回的list数据
     */
    List<String> lrange(String key, long start, long end);

    /**
     * 填充数据到list中
     * @param key key
     * @param expire 过期时间
     * @param strings 具体数据
     * @return 结果
     */
    Long lpush(String key, long expire, String... strings);

    /**
     * 判断某个key是否存在
     * @param key key
     * @return 存在返回true，否则返回false
     */
    boolean exists(String key);

    /**
     * 获取 redis 的 set 类型数据
     * @param key key
     * @return Set
     */
    Set<String> smembers(String key);

    /**
     * 设置set类型的数据
     * @param key key
     * @param expire 过期时间
     * @param member 要设置得值数组
     * @return 结果
     */
    Long sadd(String key, long expire, String... member);
}
