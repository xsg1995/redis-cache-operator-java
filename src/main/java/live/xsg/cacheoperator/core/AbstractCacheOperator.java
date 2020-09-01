package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.context.RedisCacheContext;
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

    //服务器交互接口 RedisTransporter
    protected Transporter transporter;
    //string类型操作接口
    protected StringOperator stringOperator;
    //map类型操作接口
    protected MapOperator mapOperator;
    //list类型操作接口
    protected ListOperator listOperator;
    //过滤器链构造器
    protected FilterChain filterChain = FilterChain.getInstance();
    //失败降级策略
    protected FailbackCacheOperator failbackCacheOperator = new FailbackCacheOperator(this);

    public AbstractCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(resourceLoader);
        this.transporter = transporter;
        this.stringOperator = new RedisStringOperator(this.transporter , resourceLoader);
        this.mapOperator = new RedisMapOperator(this.transporter, resourceLoader);
        this.listOperator = new RedisListOperator(this.transporter, resourceLoader);
    }

    /**
     * 预处理操作
     */
    protected void preProcess() {
        RedisCacheContext.getContext().setFuture(null);
    }
}
