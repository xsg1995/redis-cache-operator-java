package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.Codec;
import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.filter.Filter;
import live.xsg.cacheoperator.filter.FilterChainBuilder;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.transport.Transporter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 缓存操作类的抽象类，将通用方法抽取到这里
 * Created by xsg on 2020/7/26.
 */
public abstract class AbstractCacheOperator implements CacheOperator {
    //默认刷新缓存的最大时间，2分钟，单位：ms
    protected static final long DEFAULT_LOADING_KEY_EXPIRE = 2 * 60 * 1000L;
    //默认延长时间，5分钟
    protected static final long DEFAULT_EXTEND_EXPIRE = 5 * 60 * 1000L;
    //刷新缓存的最大时间
    protected long loadingKeyExpire;
    //过期时间的延长时间
    protected long extendExpire = DEFAULT_EXTEND_EXPIRE;
    //服务器交互接口 RedisTransporter
    protected Transporter transporter;
    //String类型编解码
    protected Codec stringCodec = new StringCodec();
    //异步任务执行器
    protected CacheExecutor asyncCacheExecutor = new AsyncCacheExecutor();
    //过滤器链构造器
    private FilterChainBuilder filterChainBuilder = FilterChainBuilder.getInstance();
    //过滤器链
    protected List<Filter> filters;

    public AbstractCacheOperator(Transporter transporter, long loadingKeyExpire) {
        this.transporter = transporter;
        this.loadingKeyExpire = loadingKeyExpire;

        this.buildFilter();
    }

    /**
     * 创建过滤器链
     */
    private void buildFilter() {
        this.filters = this.filterChainBuilder.build();
    }

    /**
     * 填充数据到缓存中
     * @param key key
     * @param expire 缓存过期时间
     * @param flusher 获取缓存数据
     * @return 返回最新数据
     */
    protected String doFillStringCache(String key, long expire, Refresher<String> flusher) {
        boolean isLoading = false;
        try {
            //设置正在加载key对应的数据
            isLoading = this.isLoading(key);
            //isLoading=true，则已有其他线程在刷新数据
            if (isLoading) {
                return Constants.EMPTY_STRING;
            }

            //检查是否已经有其他线程刷新完缓存
            String res = this.transporter.get(key);
            if (StringUtils.isNotBlank(res)) {
                StringCodec.StringData stringData = this.getDecodeStringData(res);
                boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());
                if (!invalid) {
                    //没有过期，已有其他线程刷新了缓存，返回缓存数据
                    return stringData.getData();
                }
            }

            String data = flusher.refresh();

            if (StringUtils.isBlank(data)) {
                data = Constants.EMPTY_STRING;
            }

            long newExpire = this.getExtendExpire(expire);
            this.transporter.set(key, newExpire, (String) this.stringCodec.encode(expire, data));

            return data;
        } finally {
            if (!isLoading) {
                //设置key已经加载完毕
                this.loadFinish(key);
            }
        }
    }

    /**
     * 延长时间过期时间
     * @param expire 原来的过期时间
     * @return 延长后的过期时间
     */
    private long getExtendExpire(long expire) {
        return expire + this.extendExpire;
    }

    /**
     * 设置key对应的数据已经加载完毕
     * @param key key
     */
    protected void loadFinish(String key) {
        this.transporter.del(Constants.LOADING_KEY + key);
    }

    /**
     * 设置key对应的数据正在加载，如果没有其他线程在刷新数据，则当前线程进行刷新
     * @param key key
     * @return 返回true，则说明已有其他线程正在刷新，返回false，则表示没有其他线程在刷新
     */
    protected boolean isLoading(String key) {
        //设置缓存最长刷新时间为 loadingKeyExpire ，在该时段内，只有一个线程刷新缓存
        long expire = this.loadingKeyExpire;
        int res = this.transporter.setIfNotExist(Constants.LOADING_KEY + key, key, expire);

        return res == Constants.RESULT_FAILURE;
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
     * 从编码后的字符串中，解码获取数据
     * @param data 编码后的字符串
     * @return 解码后的结果
     */
    protected StringCodec.StringData getDecodeStringData(String data) {
        return (StringCodec.StringData) this.stringCodec.decode(data);
    }

    /**
     * 添加过滤器
     * @param filter 过滤器
     */
    protected void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    /**
     * 调用所有过滤器链的前置处理
     * @param key key
     * @return true，继续执行后续逻辑；false，不执行后续逻辑
     */
    protected boolean preFilter(String key) {
        for (Filter filter : filters) {
            if (!filter.preFilter(key)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 调用所有过滤器的后置处理
     * @param key key
     * @param result 返回结果
     */
    protected void postFilter(String key, Object result) {
        for (Filter filter : filters) {
            try {
                filter.postFilter(key, result);
            } catch (Exception e) {
                //失败不影响后续逻辑
                e.printStackTrace();
            }
        }
    }
}
