package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.common.Constants;
import live.xsg.cacheoperator.exception.RetryRecoverException;
import live.xsg.cacheoperator.mock.Mock;
import live.xsg.cacheoperator.mock.MockRegister;
import live.xsg.cacheoperator.resource.Resource;
import live.xsg.cacheoperator.resource.ResourceRegister;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 失败降级策略
 * Created by xsg on 2020/8/5.
 */
public class FailbackCacheOperator {

    private static final int DEFAULT_RETRY_TIME = 5;
    //定时任务运行周期
    private static final long DEFAULT_RETRY_PERIOD = 30 * 1000L;

    private CacheOperator cacheOperator;
    //降级控制开关
    private AtomicBoolean block = new AtomicBoolean();
    //失败重试次数
    private int retryTime;
    //redis重试频率
    private long retryPeriod;
    //当前失败重试次数
    private AtomicInteger currRetryTime = new AtomicInteger();
    //定时检测redis是否恢复
    private ScheduledExecutorService checkExecutor = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> checkFuture;
    //redis底层连接
    private Transporter transporter;
    //mock降级实现类
    private MockRegister mockRegister = MockRegister.getInstance();
    //获取资源数据
    protected Resource resource;

    public FailbackCacheOperator(CacheOperator cacheOperator) {
        this.cacheOperator = cacheOperator;

        if (cacheOperator instanceof ResourceRegister) {
            this.resource = ((ResourceRegister) cacheOperator).getResource();
            this.retryTime = this.resource.getInt(Constants.RETRY_TIME, DEFAULT_RETRY_TIME);
            this.retryPeriod = this.resource.getLong(Constants.RETRY_PERIOD, DEFAULT_RETRY_PERIOD);
        }

        this.transporter = new RedisTransporter();
    }

    public Object invoke(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        if (this.block.get()) {
            //中断后，走降级逻辑
            return doMock(method, args);
        }

        while (this.currRetryTime.get() < this.retryTime) {
            try {
                Object result = method.invoke(this.cacheOperator, args);
                //重置计算器
                this.currRetryTime.set(0);
                return result;
            } catch (Throwable t) {
                t.printStackTrace();
                this.currRetryTime.incrementAndGet();
            }
        }

        this.block.set(true);
        this.addScheduleCheckRecover();
        return doMock(method, args);
    }

    /**
     * 定时检测redis是否已经恢复
     */
    protected void addScheduleCheckRecover() {
        if (this.checkFuture == null) {
            this.checkFuture = checkExecutor.scheduleWithFixedDelay(() -> {
                try {
                    checkRecover();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }, retryPeriod, retryPeriod, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 重试redis是否恢复
     */
    protected void checkRecover() throws RetryRecoverException {
        if (this.block.get()) {
            //测试连接是否恢复
            this.test();
            //恢复正常操作
            this.recover();
        }
    }

    /**
     * redis恢复正常
     */
    protected void recover() {
        //连接恢复
        this.block.set(false);
        //重置次数
        this.currRetryTime.set(0);
        if (this.checkFuture != null) {
            this.checkFuture.cancel(true);
            this.checkFuture = null;
        }
    }

    /**
     * 测试redis是否恢复
     */
    protected void test() throws RetryRecoverException {
        String key = "test_key";
        String value = "test_value";
        try {
            String result = this.transporter.set(key, 1, value);
            if (!Constants.OK.equals(result)) {
                throw new RetryRecoverException("redis test recover error");
            }
        } catch (Throwable t) {
            throw new RetryRecoverException("redis test recover error", t);
        }
    }

    /**
     * redis失败后走降级逻辑=
     * @return 返回降级逻辑的结果
     */
    private Object doMock(Method method, Object[] args) {
        String key = (String) args[0];
        //获取mock列表
        Iterator<Mock> mockCacheOperators = this.mockRegister.getMockCacheOperators();
        while (mockCacheOperators.hasNext()) {
            Mock mock = mockCacheOperators.next();
            //执行mock逻辑
            Object result = mock.mock(key, method);
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
