package live.xsg.cacheoperator;

import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.PropertiesResourceLoader;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * redis缓存操作器
 * Created by xsg on 2020/7/20.
 */
public class RedisCacheOperator extends AbstractCacheOperator implements CacheOperator {

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
        super(transporter, resourceLoader);
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

    @Override
    public Map<String, String> getAllMap(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.cacheOperatorProxy.getAllMap(key, expire, flusher);
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
            return this.stringOperator.getString(key, expire, flusher);
        }

        @Override
        public String getStringAsync(String key, long expire, Refresher<String> flusher) {
            return this.stringOperator.getStringAsync(key, expire, flusher);
        }

        @Override
        public String getStringAsync(String key, long expire, Refresher<String> flusher, Executor executor) {
            return this.stringOperator.getStringAsync(key, expire, flusher, executor);
        }

        @Override
        public String getString(String key) {
            return this.stringOperator.getString(key);
        }

        @Override
        public Map<String, String> getAllMap(String key) {
            return this.mapOperator.getAllMap(key);
        }

        @Override
        public Map<String, String> getAllMap(String key, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.getAllMap(key, expire, flusher);
        }
    }

}
