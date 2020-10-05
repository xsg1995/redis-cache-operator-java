package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.context.RedisCacheContext;
import live.xsg.cacheoperator.filter.FilterChain;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.resource.DefaultResourceRegister;
import live.xsg.cacheoperator.transport.Transporter;

/**
 * 缓存操作类的抽象类，将通用方法抽取到这里
 * Created by xsg on 2020/7/26.
 */
public abstract class AbstractCacheOperator extends DefaultResourceRegister implements CacheOperator {

    //string类型操作接口
    protected StringOperator stringOperator;
    //map类型操作接口
    protected MapOperator mapOperator;
    //list类型操作接口
    protected ListOperator listOperator;
    //set 类型操作接口
    protected SetOperator setOperator;
    //过滤器链构造器
    protected FilterChain filterChain = FilterChain.getInstance();
    //失败降级策略
    protected FailbackCacheOperator failbackCacheOperator;

    public AbstractCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.stringOperator = new RedisStringOperator(transporter , resourceLoader);
        this.mapOperator = new RedisMapOperator(transporter, resourceLoader);
        this.listOperator = new RedisListOperator(transporter, resourceLoader);
        this.setOperator = new RedisSetOperator(transporter, resourceLoader);
        this.failbackCacheOperator = new FailbackCacheOperator(this);
    }

    /**
     * 预处理操作
     */
    protected void preProcess() {
        RedisCacheContext.getContext().setFuture(null);
    }
}
