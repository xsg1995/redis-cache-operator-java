package live.xsg.cacheoperator;

import live.xsg.cacheoperator.CacheOperator;
import live.xsg.cacheoperator.codec.Codec;
import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.CodecFactory;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.context.RedisCacheContext;
import live.xsg.cacheoperator.core.MapOperator;
import live.xsg.cacheoperator.core.RedisMapOperator;
import live.xsg.cacheoperator.core.RedisStringOperator;
import live.xsg.cacheoperator.core.StringOperator;
import live.xsg.cacheoperator.filter.FilterChain;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.resource.DefaultResourceRegister;
import live.xsg.cacheoperator.support.FailbackCacheOperator;
import live.xsg.cacheoperator.transport.Transporter;

/**
 * 缓存操作类的抽象类，将通用方法抽取到这里
 * Created by xsg on 2020/7/26.
 */
public abstract class AbstractCacheOperator extends DefaultResourceRegister implements CacheOperator {
    //默认刷新缓存的最大时间，2分钟，单位：ms
    protected static final long DEFAULT_LOADING_KEY_EXPIRE = 2 * 60 * 1000L;
    //默认延长时间，5分钟
    protected static final long DEFAULT_EXTEND_EXPIRE = 5 * 60 * 1000L;
    //刷新缓存的最大时间
    protected long loadingKeyExpire;
    //过期时间的延长时间
    protected long extendExpire;
    //服务器交互接口 RedisTransporter
    protected Transporter transporter;
    //string类型操作接口
    protected StringOperator stringOperator;
    //map类型操作接口
    protected MapOperator mapOperator;
    //过滤器链构造器
    protected FilterChain filterChain = FilterChain.getInstance();
    //失败降级策略
    protected FailbackCacheOperator failbackCacheOperator = new FailbackCacheOperator(this);

    public AbstractCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.transporter = transporter;
        this.stringOperator = new RedisStringOperator(this.transporter, this);
        this.mapOperator = new RedisMapOperator(this.transporter, this);
        this.loadingKeyExpire = this.resource.getLong(Constants.LOADING_KEY_EXPIRE, DEFAULT_LOADING_KEY_EXPIRE);
        this.extendExpire = this.resource.getLong(Constants.EXTEND_EXPIRE, DEFAULT_EXTEND_EXPIRE);
    }

    @Override
    public long getExtendExpire(long expire) {
        return expire + this.extendExpire;
    }

    @Override
    public void loadFinish(String key) {
        this.transporter.del(Constants.LOADING_KEY + key);
    }

    @Override
    public boolean isLoading(String key) {
        //设置缓存最长刷新时间为 loadingKeyExpire ，在该时段内，只有一个线程刷新缓存
        int res = this.transporter.setIfNotExist(Constants.LOADING_KEY + key, key, this.loadingKeyExpire);

        return res == Constants.RESULT_FAILURE;
    }

    @Override
    public boolean isInvalid(long absoluteExpireTime) {
        //Constants.ABSOLUTE_EXPIRE_TIME 为了兼容没有编码过的数据，实际过期时间由expire设置
        return absoluteExpireTime != Constants.ABSOLUTE_EXPIRE_TIME && absoluteExpireTime <= System.currentTimeMillis();
    }

    @Override
    public Object getEncodeData(long expire, Object data, CodecEnum codecEnum) {
        Codec codec = CodecFactory.getByType(codecEnum);

        return codec.encode(expire, data);
    }

    @Override
    public Object getDecodeData(Object data, CodecEnum codecEnum) {
        Codec codec = CodecFactory.getByType(codecEnum);

        return codec.decode(data);
    }

    /**
     * 预处理操作
     */
    protected void preProcess() {
        RedisCacheContext.getContext().setFuture(null);
    }
}
