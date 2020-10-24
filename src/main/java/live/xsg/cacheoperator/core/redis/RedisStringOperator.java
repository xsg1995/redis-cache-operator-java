package live.xsg.cacheoperator.core.redis;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.data.StringData;
import live.xsg.cacheoperator.core.StringOperator;
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
            res = cacheExecutor.executor(() -> this.fillCache(new FillCache<String>() {

                @Override
                public String getKey() {
                    return key;
                }

                @Override
                public String getIgnoreValidData() {
                    return (String) blockIfNeed(this.getKey());
                }

                @Override
                public String getCacheData() {
                    //执行具体的获取缓存数据逻辑
                    String data = flusher.refresh();
                    //对过期时间进行延长
                    long newExpire = getExtendExpire(expire);
                    //对数据进行编码操作
                    String encode = (String) getEncodeData(expire, data, CodecEnum.STRING);
                    //填充缓存
                    transporter.set(this.getKey(), newExpire, encode);
                    return data;
                }
            }));

        } else {
            //缓存中存在数据且未过期
            res = stringData.getData();
        }

        return res;
    }

    @Override
    protected String getDataIgnoreValid(String key) {
        String res = this.transporter.get(key);
        if (StringUtils.isBlank(res)) return null;

        StringData stringData = (StringData) this.getDecodeData(res, CodecEnum.STRING);
        return stringData.getData();
    }


}
