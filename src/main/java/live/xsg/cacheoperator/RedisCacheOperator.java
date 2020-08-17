package live.xsg.cacheoperator;

import live.xsg.cacheoperator.codec.CodecEnum;
import live.xsg.cacheoperator.codec.MapCodec;
import live.xsg.cacheoperator.codec.StringCodec;
import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.executor.AsyncCacheExecutor;
import live.xsg.cacheoperator.executor.CacheExecutor;
import live.xsg.cacheoperator.executor.SyncCacheExecutor;
import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.PropertiesResourceLoader;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * redis缓存操作器
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheOperator implements CacheOperator {

    private CacheOperator cacheOperatorProxy;

    public RedisCacheOperator() {
        this(new RedisTransporter(), new PropertiesResourceLoader());
    }

    public RedisCacheOperator(Transporter transporter) {
        this(transporter, new PropertiesResourceLoader());
    }

    public RedisCacheOperator(ResourceLoader resourceLoader) {
        this(new RedisTransporter(), resourceLoader);
    }

    public RedisCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
        this.cacheOperatorProxy = this.newProxy(new InnerRedisCacheOperator(transporter, resourceLoader));
    }

    /**
     * 创建代理
     */
    public CacheOperator newProxy(InnerRedisCacheOperator cacheOperator) {
        return (CacheOperator) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class[] {CacheOperator.class}, cacheOperator);
    }

    @Override
    public String getString(String key, long expire, Refresher<String> flusher) {
        return this.cacheOperatorProxy.getString(key, expire, flusher);
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher) {
        return this.cacheOperatorProxy.getStringAsync(key, expire, flusher);
    }

    @Override
    public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor) {
        return this.cacheOperatorProxy.getStringAsync(key, expire, flusher, executor);
    }

    @Override
    public String getString(String key) {
        return this.cacheOperatorProxy.getString(key);
    }

    @Override
    public Map<String, String> getAllMap(String key) {
        return this.cacheOperatorProxy.getAllMap(key);
    }

    /**
     * 内部类，实现 InvocationHandler，实现代理，控制访问
     */
    static class InnerRedisCacheOperator extends AbstractCacheOperator implements CacheOperator, InvocationHandler {

        public InnerRedisCacheOperator(Transporter transporter, ResourceLoader resourceLoader) {
            super(transporter, resourceLoader);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();
            Class<?>[] parameterTypes = method.getParameterTypes();

            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            if ("toString".equals(methodName) && parameterTypes.length == 0) {
                return this.toString();
            }
            if ("hashCode".equals(methodName) && parameterTypes.length == 0) {
                return this.hashCode();
            }
            if ("equals".equals(methodName) && parameterTypes.length == 1) {
                return this.equals(args[0]);
            }
            String key = (String) args[0];
            //过滤器链前置处理
            if (!this.filterChain.preFilter(key)) {
                //过滤器链后置处理
                this.filterChain.postFilter(key, null);
                return null;
            }

            Object result = this.failbackCacheOperator.invoke(method, args);

            //过滤器链后置处理
            this.filterChain.postFilter(key, result);

            return result;
        }

        @Override
        public String getString(String key, long expire, Refresher<String> flusher) {
            return this.getString(key, expire, flusher, new SyncCacheExecutor());
        }

        @Override
        public String getStringAsync(String key, long expire, Refresher<String> flusher) {
            return this.getString(key, expire, flusher, this.asyncCacheExecutor);
        }

        @Override
        public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor) {
            return this.getString(key, expire, flusher, new AsyncCacheExecutor(executor));
        }

        private String getString(String key, long expire, Refresher<String> flusher, CacheExecutor cacheExecutor) {
            String res = this.transporter.getString(key);

            if (StringUtils.isBlank(res)) {
                //缓存中不存在数据，获取数据，放入缓存
                res = (String) cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
            } else {
                //缓存中存在数据，判断缓存是否已经过期
                StringCodec.StringData stringData = (StringCodec.StringData) this.getDecodeData(res, CodecEnum.STRING);
                boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

                if (invalid) {
                    //缓存过期，刷新缓存
                    res = (String) cacheExecutor.executor(() -> this.doFillStringCache(key, expire, flusher));
                    //如果有其他线程在刷新缓存，则返回现在缓存中的值
                    if (Constants.EMPTY_STRING.equals(res) && stringData.getData() != null) {
                        res = stringData.getData();
                    }
                } else {
                    //未过期
                    res = stringData.getData();
                }
            }
            return res;
        }

        @Override
        public String getString(String key) {
            String res = this.transporter.getString(key);
            StringCodec.StringData stringData = (StringCodec.StringData) this.getDecodeData(res, CodecEnum.STRING);
            boolean invalid = this.isInvalid(stringData.getAbsoluteExpireTime());

            if (invalid) {
                //缓存数据过期
                return Constants.EMPTY_STRING;
            }
            return stringData.getData();
        }

        @Override
        public Map<String, String> getAllMap(String key) {
            Map<String, String> map = this.transporter.getAllMap(key);
            MapCodec.MapData mapData = (MapCodec.MapData) this.getDecodeData(map, CodecEnum.MAP);
            boolean invalid = this.isInvalid(mapData.getAbsoluteExpireTime());

            if (invalid) {
                return Constants.EMPTY_MAP;
            }

            return mapData.getData();
        }
    }

}
