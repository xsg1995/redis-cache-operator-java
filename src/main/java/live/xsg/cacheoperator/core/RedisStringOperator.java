package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.data.StringData;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.FutureAdapter;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * string类型操作实现
 * Created by xsg on 2020/8/17.
 */
public class RedisStringOperator extends AbstractRedisOperator implements StringOperator {

    //异步任务执行器
    protected CacheExecutor<String> asyncCacheExecutor = new AsyncCacheExecutor<String>();

    public RedisStringOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    public String get(String key, long expire, Refresher<String> flusher) {
        return this.get(key, expire, flusher, new SyncCacheExecutor<>()).getData();
    }

    @Override
    public Future<String> getAsync(String key, long expire, Refresher<String> flusher) {
        return this.get(key, expire, flusher, this.asyncCacheExecutor);
    }

    @Override
    public Future<String> getAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService) {
        return this.get(key, expire, flusher, new AsyncCacheExecutor<>(executorService));
    }

    @Override
    public void del(String key) {
        this.transporter.del(key);
    }

    private FutureAdapter<String> get(String key, long expire,
                                      Refresher<String> flusher,
                                      CacheExecutor<String> cacheExecutor) {
        String res = this.transporter.get(key);

        //数据解码
        StringData stringData = (StringData) this.getDecodeData(res, CodecEnum.STRING);
        boolean invalid = stringData.isInvalid();

        if (invalid) {
            //缓存过期，则重新刷新缓存
            return (FutureAdapter<String>) cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
        } else {
            //缓存中存在数据且未过期
            return new FutureAdapter<>(stringData.getData());
        }
    }

    /**
     * 执行 Refresher 获取数据，并将数据填充到缓存中
     *
     * @param key     key
     * @param expire  缓存过期时间
     * @param flusher 获取缓存数据
     * @return 返回最新数据
     */
    protected String doFillStringCache(String key, long expire, Refresher<String> flusher) {
        boolean lock = false;
        try {
            //获取锁
            lock = this.tryLock(key);
            if (!lock) {
                //没有获取到锁，走阻塞降级策略
                return (String) this.blockIfNeed(key);
            }

            //执行具体的获取缓存数据逻辑
            String data = flusher.refresh();
            //对过期时间进行延长
            long newExpire = this.getExtendExpire(expire);
            //对数据进行编码操作
            String encode = (String) this.getEncodeData(expire, data, CodecEnum.STRING);
            //填充缓存
            this.transporter.set(key, newExpire, encode);

            return data;
        } finally {
            if (lock) {
                //释放锁
                this.unlock(key);
            }
        }
    }

    @Override
    protected String getDataIgnoreValid(String key) {
        String res = this.transporter.get(key);
        if (StringUtils.isBlank(res)) return null;

        StringData stringData = (StringData) this.getDecodeData(res, CodecEnum.STRING);
        return stringData.getData();
    }


}
