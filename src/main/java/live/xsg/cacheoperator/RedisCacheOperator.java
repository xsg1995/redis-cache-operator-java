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
import java.util.concurrent.ExecutorService;

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
    public String get(String key, long expire, Refresher<String> flusher) {
        return this.cacheOperatorProxy.get(key, expire, flusher);
    }

    @Override
    public String getAsync(String key, long expire, Refresher<String> flusher) {
        return this.cacheOperatorProxy.getAsync(key, expire, flusher);
    }

    @Override
    public String getAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService) {
        return this.cacheOperatorProxy.getAsync(key, expire, flusher, executorService);
    }

    @Override
    public String get(String key) {
        return this.cacheOperatorProxy.get(key);
    }

    @Override
    public Map<String, String> hgetAll(String key) {
        return this.cacheOperatorProxy.hgetAll(key);
    }

    @Override
    public Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.cacheOperatorProxy.hgetAll(key, expire, flusher);
    }

    @Override
    public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher) {
        return this.cacheOperatorProxy.hgetAllAsync(key, expire, flusher);
    }

    @Override
    public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService) {
        return this.cacheOperatorProxy.hgetAllAsync(key, expire, flusher, executorService);
    }

    @Override
    public String hget(String key, String field) {
        return this.cacheOperatorProxy.hget(key, field);
    }

    @Override
    public String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher) {
        return this.hget(key, field, expire, flusher);
    }

    @Override
    public void del(String key) {
        this.cacheOperatorProxy.del(key);
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
            //预处理操作
            this.preProcess();

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
        public void del(String key) {
            this.transporter.del(key);
        }

        @Override
        public String get(String key, long expire, Refresher<String> flusher) {
            return this.stringOperator.get(key, expire, flusher);
        }

        @Override
        public String getAsync(String key, long expire, Refresher<String> flusher) {
            return this.stringOperator.getAsync(key, expire, flusher);
        }

        @Override
        public String getAsync(String key, long expire, Refresher<String> flusher, ExecutorService executorService) {
            return this.stringOperator.getAsync(key, expire, flusher, executorService);
        }

        @Override
        public String get(String key) {
            return this.stringOperator.get(key);
        }

        @Override
        public Map<String, String> hgetAll(String key) {
            return this.mapOperator.hgetAll(key);
        }

        @Override
        public Map<String, String> hgetAll(String key, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hgetAll(key, expire, flusher);
        }

        @Override
        public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hgetAllAsync(key, expire, flusher);
        }

        @Override
        public Map<String, String> hgetAllAsync(String key, long expire, Refresher<Map<String, String>> flusher, ExecutorService executorService) {
            return this.mapOperator.hgetAllAsync(key, expire, flusher, executorService);
        }

        @Override
        public String hget(String key, String field) {
            return this.mapOperator.hget(key, field);
        }

        @Override
        public String hget(String key, String field, long expire, Refresher<Map<String, String>> flusher) {
            return this.mapOperator.hget(key, field, expire, flusher);
        }
    }

}
