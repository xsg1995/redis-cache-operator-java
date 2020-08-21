package live.xsg.cacheoperator.support;

import java.util.concurrent.TimeUnit;

/**
 * 时间工具
 * Created by xsg on 2020/8/21.
 */
public class TimeUtils {

    /**
     * 睡眠指定时间后返回
     * @param timeOut timeOut
     * @param timeUnit timeUnit
     */
    public static void sleep(long timeOut, TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeOut);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
