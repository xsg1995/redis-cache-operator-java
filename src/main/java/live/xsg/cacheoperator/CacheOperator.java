package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.core.MapOperator;
import live.xsg.cacheoperator.core.StringOperator;

/**
 * 缓存操作器
 * Created by xsg on 2020/7/20.
 */
public interface CacheOperator extends StringOperator, MapOperator {

    /**
     * 删除key
     * @param key key
     */
    void del(String key);

    /**
     * 设置key对应的数据正在加载，如果没有其他线程在刷新数据，则当前线程进行刷新
     * @param key key
     * @return 返回true，则说明已有其他线程正在刷新，返回false，则表示没有其他线程在刷新
     */
    boolean isLoading(String key);

    /**
     * 设置key对应的数据已经加载完毕
     * @param key key
     */
    void loadFinish(String key);

    /**
     * 判断缓存的绝对过期时间是否过期
     * @param absoluteExpireTime 缓存绝对过期时间，单位：ms
     * @return 返回true，则过期，返回false，则未过期
     */
    boolean isInvalid(long absoluteExpireTime);

    /**
     * 延长时间过期时间
     * @param expire 原来的过期时间
     * @return 延长后的过期时间
     */
    long getExtendExpire(long expire);

    /**
     * 从编码后的数据中返回解码后的数据
     * @param data 编码后的数据
     * @param codecEnum CodecEnum
     * @return 解码后的数据
     */
   Object getDecodeData(Object data, CodecEnum codecEnum);

    /**
     * 获取编码数据
     * @param expire 过期时间
     * @param data 编码前的数据
     * @param codecEnum codecEnum
     * @return 编码后的数据
     */
    Object getEncodeData(long expire, Object data, CodecEnum codecEnum);
}
