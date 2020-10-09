package live.xsg.cacheoperator.core;

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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    //默认获取缓存的重试间隔
    protected static final long DEFAULT_RETRY_INTERVAL = 100L;

    //刷新缓存的最大时间
    protected long loadingKeyExpire;
    //过期时间的延长时间
    protected long extendExpire;
    //缓存无数据时，线程最大阻塞时间
    protected long blockTime;
    //缓存无数据时的重试时间间隔
    protected long retryInterval;
    //服务器交互接口 RedisTransporter
    protected Transporter transporter;
    //本地锁，一个 key 对应一个 Lock
    private Map<String, Lock> localLock = new ConcurrentHashMap<>();

    public AbstractRedisOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.transporter = transporter;
        this.loadingKeyExpire = this.resource.getLong(Constants.LOADING_KEY_EXPIRE, DEFAULT_LOADING_KEY_EXPIRE);
        this.extendExpire = this.resource.getLong(Constants.EXTEND_EXPIRE, DEFAULT_EXTEND_EXPIRE);
        this.blockTime = this.resource.getLong(Constants.BLOCK_TIME, DEFAULT_BLOCK_TIME);
        this.retryInterval = this.resource.getLong(Constants.RETRY_INTERVAL, DEFAULT_RETRY_INTERVAL);
    }

    /**
     * 尝试获取锁，如果获取到锁，则可以执行缓存刷新操作
     * @param key key
     * @return 返回true，则说明已经获取到锁；返回false，则说明没有获取到锁
     */
    protected boolean tryLock(String key) {
        Lock localLock = this.getLocalLock(key);
        boolean lock = localLock.tryLock();
        try {
            //获取到本地锁才执行远程分布式锁的获取
            if (lock) {
                //设置缓存最长刷新时间为 loadingKeyExpire ，在该时段内，只有一个线程可以获取到锁
                return this.transporter.setIfNotExist(Constants.LOADING_KEY + key, key, this.loadingKeyExpire);
            }
        } finally {
            if (lock) {
                localLock.unlock();
            }
        }
        return false;
    }

    /**
     * 释放刷新缓存的锁
     * @param key key
     */
    public void unlock(String key) {
        this.transporter.del(Constants.LOADING_KEY + key);
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
     * 如果在指定时间内没有获取到数据，则执行Mock降级逻辑
     * @param key key
     * @return 获取缓存中的数据
     */
    protected Object blockIfNeed(String key) {
        Object res = null;

        Lock localLock = this.getLocalLock(key);
        localLock.lock();
        try {
            //缓存中无数据，则循环遍历获取缓存中的数据
            long startTime = System.currentTimeMillis();
            while ((res = this.getDataIgnoreValid(key)) == null) {
                TimeUtils.sleep(retryInterval, TimeUnit.MILLISECONDS);

                if (System.currentTimeMillis() - startTime > blockTime) break;
            }

            if (res != null) return res;

            //阻塞一定时间后还是获取不到数据，则执行降级逻辑
            MockRegister mockRegister = MockRegister.getInstance();
            Iterator<Mock> mockCacheOperators = mockRegister.getMockCacheOperators();
            while (mockCacheOperators.hasNext()) {
                Mock mock = mockCacheOperators.next();
                //执行mock逻辑
                Object result = mock.mock(key, null);
                if (result != null) {
                    return result;
                }
            }
        } finally {
            localLock.unlock();
        }

        return null;
    }

    /**
     * 获取 key 对应的本地锁 Lock
     * 一个 key 对应一个 Lock
     * @param key key
     * @return Lock
     */
    private Lock getLocalLock(String key) {
        this.localLock.putIfAbsent(key, new ReentrantLock());
        return this.localLock.get(key);
    }

    /**
     * 获取数据，缓存中无数据，则返回null，那么将会循环获取缓存中
     * @param key key
     * @return 缓存中的数据
     */
    protected abstract Object getDataIgnoreValid(String key);
}
