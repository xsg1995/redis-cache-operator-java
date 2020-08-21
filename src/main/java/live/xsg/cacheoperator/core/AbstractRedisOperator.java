package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.codec.Codec;
import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.CodecFactory;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.mock.Mock;
import live.xsg.cacheoperator.mock.MockRegister;
import live.xsg.cacheoperator.resource.DefaultResourceRegister;
import live.xsg.cacheoperator.support.TimeUtils;
import live.xsg.cacheoperator.transport.Transporter;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

/**
 * redis操作的父类
 * Created by xsg on 2020/8/21.
 */
public abstract class AbstractRedisOperator extends DefaultResourceRegister {
    //默认刷新缓存的最大时间，2分钟，单位：ms
    protected static final long DEFAULT_LOADING_KEY_EXPIRE = 2 * 60 * 1000L;
    //默认延长时间，5分钟
    protected static final long DEFAULT_EXTEND_EXPIRE = 5 * 60 * 1000L;
    //默认阻塞时间，1 s
    protected static final long DEFAULT_BLOCK_TIME = 1000L;

    //刷新缓存的最大时间
    protected long loadingKeyExpire;
    //过期时间的延长时间
    protected long extendExpire;
    //缓存无数据时，线程最大阻塞时间
    protected long blockTime;

    //服务器交互接口 RedisTransporter
    protected Transporter transporter;
    //缓存操作
    protected CacheOperator cacheOperator;


    public AbstractRedisOperator(Transporter transporter, CacheOperator cacheOperator, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.transporter = transporter;
        this.cacheOperator = cacheOperator;
        this.loadingKeyExpire = this.resource.getLong(Constants.LOADING_KEY_EXPIRE, DEFAULT_LOADING_KEY_EXPIRE);
        this.extendExpire = this.resource.getLong(Constants.EXTEND_EXPIRE, DEFAULT_EXTEND_EXPIRE);
        this.blockTime = this.resource.getLong(Constants.BLOCK_TIME, DEFAULT_BLOCK_TIME);
    }

    /**
     * 设置key对应的数据正在加载，如果没有其他线程在刷新数据，则当前线程进行刷新
     * @param key key
     * @return 返回true，则说明已有其他线程正在刷新，返回false，则表示没有其他线程在刷新
     */
    protected boolean isLoading(String key) {
        //设置缓存最长刷新时间为 loadingKeyExpire ，在该时段内，只有一个线程刷新缓存
        int res = this.transporter.setIfNotExist(Constants.LOADING_KEY + key, key, this.loadingKeyExpire);

        return res == Constants.RESULT_FAILURE;
    }

    /**
     * 设置key对应的数据已经加载完毕
     * @param key key
     */
    public void loadFinish(String key) {
        this.transporter.del(Constants.LOADING_KEY + key);
    }

    /**
     * 判断缓存的绝对过期时间是否过期
     * @param absoluteExpireTime 缓存绝对过期时间，单位：ms
     * @return 返回true，则过期，返回false，则未过期
     */
    protected boolean isInvalid(long absoluteExpireTime) {
        //Constants.ABSOLUTE_EXPIRE_TIME 为了兼容没有编码过的数据，实际过期时间由expire设置
        return absoluteExpireTime != Constants.ABSOLUTE_EXPIRE_TIME && absoluteExpireTime <= System.currentTimeMillis();
    }

    /**
     * 延长时间过期时间
     * @param expire 原来的过期时间
     * @return 延长后的过期时间
     */
    public long getExtendExpire(long expire) {
        return expire + this.extendExpire;
    }

    /**
     * 从编码后的数据中返回解码后的数据
     * @param data 编码后的数据
     * @param codecEnum CodecEnum
     * @return 解码后的数据
     */
    public Object getDecodeData(Object data, CodecEnum codecEnum) {
        Codec codec = CodecFactory.getByType(codecEnum);

        return codec.decode(data);
    }

    /**
     * 获取编码数据
     * @param expire 过期时间
     * @param data 编码前的数据
     * @param codecEnum codecEnum
     * @return 编码后的数据
     */
    public Object getEncodeData(long expire, Object data, CodecEnum codecEnum) {
        Codec codec = CodecFactory.getByType(codecEnum);

        return codec.encode(expire, data);
    }

    /**
     * 如果缓存中有数据，则返回缓存中的数据；如果缓存中无数据，则循环遍历查询缓存中的数据；
     * 如果在指定时间内没有获取到数据，则只需Mock降级逻辑
     * @param key key
     * @return 获取缓存中的数据
     */
    protected Object blockIfNeed(String key) {
        Object res = this.getDataIgnoreValid(key);
        if (res != null) return res;

        //缓存中无数据，则循环遍历获取缓存中的数据
        long sleepTime = 100L;
        long currBlockTime = 0L;
        while (currBlockTime < blockTime) {
            long start = System.currentTimeMillis();
            TimeUtils.sleep(sleepTime, TimeUnit.MILLISECONDS);
            res = this.getDataIgnoreValid(key);

            if (res != null) return res;

            currBlockTime += System.currentTimeMillis() - start;
        }

        MockRegister mockRegister = MockRegister.getInstance();
        Iterator<Mock> mockCacheOperators = mockRegister.getMockCacheOperators();
        while (mockCacheOperators.hasNext()) {
            Mock mock = mockCacheOperators.next();
            //执行mock逻辑
            Object result = mock.mock(key, this.cacheOperator, null);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * 获取数据，缓存中无数据，则返回null，那么将会循环获取缓存中
     * @param key key
     * @return 缓存中的数据
     */
    protected abstract Object getDataIgnoreValid(String key);
}
