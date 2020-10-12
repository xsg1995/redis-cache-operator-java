package live.xsg.cacheoperator.core.redis;

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
import java.util.concurrent.atomic.AtomicInteger;
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
    private static Map<String, Lock> LOCAL_LOCK = new ConcurrentHashMap<>();
    //REFRESH_LOCK 的 key 为当前操作的 redis 的 key， AtomicInteger 为 0 ，则说明当前 key 没有在刷新；为 1 则说明该 key 正在刷新
    private static Map<String, AtomicInteger> REFRESH_LOCK = new ConcurrentHashMap<>();

    public AbstractRedisOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.transporter = transporter;
        this.loadingKeyExpire = this.resource.getLong(Constants.LOADING_KEY_EXPIRE, DEFAULT_LOADING_KEY_EXPIRE);
        this.extendExpire = this.resource.getLong(Constants.EXTEND_EXPIRE, DEFAULT_EXTEND_EXPIRE);
        this.blockTime = this.resource.getLong(Constants.BLOCK_TIME, DEFAULT_BLOCK_TIME);
        this.retryInterval = this.resource.getLong(Constants.RETRY_INTERVAL, DEFAULT_RETRY_INTERVAL);
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
     * 缓存无数据时，填充缓存逻辑
     * @param fillCache 填充缓存的数据接口
     * @param <T> 要填充到缓存的数据
     * @return 返回填充的数据
     */
    protected <T> T fillCache(FillCache<T> fillCache) {
        boolean lock = false;
        String key = fillCache.getKey();
        AtomicInteger refreshLock = null;
        try {
            //获取锁
            lock = this.tryLock(key);
            if (!lock) {
                //没有获取到锁，走阻塞降级策略
                return fillCache.getIgnoreValidData();
            }

            refreshLock = this.getRefreshLock(key);
            if (refreshLock.compareAndSet(0, 1)) {
                //执行具体的获取缓存数据逻辑
                return fillCache.getCacheData();
            }

        } finally {
            if (lock) {
                //释放锁
                this.unlock(key);
                if (refreshLock != null) {
                    refreshLock.set(0);
                }
            }
        }

        //正常逻辑，不会走到这里
        return fillCache.getIgnoreValidData();
    }

    /**
     * 尝试获取锁，如果获取到锁，则可以执行缓存刷新操作
     * @param key key
     * @return 返回true，则说明已经获取到锁；返回false，则说明没有获取到锁
     */
    protected boolean tryLock(String key) {
        //判断当前 key 是否正在刷新
        AtomicInteger refreshLock = this.getRefreshLock(key);
        if (refreshLock.get() == 1) {
            return false;
        }

        //本地锁，避免频繁调用分布式锁
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
        LOCAL_LOCK.putIfAbsent(key, new ReentrantLock());
        return LOCAL_LOCK.get(key);
    }

    /**
     * 获取正在刷新的原子变量
     * 一个 key 对应一个 AtomicInteger
     * @param key key
     * @return AtomicInteger
     */
    private AtomicInteger getRefreshLock(String key) {
        REFRESH_LOCK.putIfAbsent(key, new AtomicInteger());
        return REFRESH_LOCK.get(key);
    }


    /**
     * 获取数据，缓存中无数据，则返回null，那么将会循环获取缓存中
     * @param key key
     * @return 缓存中的数据
     */
    protected abstract Object getDataIgnoreValid(String key);

    /**
     * 实际填充缓存的接口
     * @param <T> 填充的数据类型
     */
    interface FillCache<T> {
        /**
         * 获取要操作的key
         * @return key
         */
        String getKey();

        /**
         * 获取缓存中的数据，忽略缓存是否过期，只要有数据则返回，key = this.getKey()
         * @return 缓存中的数据
         */
        T getIgnoreValidData();

        /**
         * 获取实际要填充到缓存中的数据
         * @return 调用 Refresher.refresh 获取要填入到缓存中的数据
         */
        T getCacheData();

    }
}
