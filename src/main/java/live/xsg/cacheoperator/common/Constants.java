package live.xsg.cacheoperator.common;

import java.util.*;

/**
 * 系统常量
 * Created by xsg on 2020/7/21.
 */
public class Constants {

    //空字符串
    public final static String EMPTY_STRING = "";
    //空Map
    public final static Map<String, String> EMPTY_MAP = Collections.unmodifiableMap(new HashMap<>());
    //空list
    public final static List<String> EMPTY_LIST = Collections.unmodifiableList(new ArrayList<>());

    //设置正在刷新缓存标识前置
    public static String LOADING_KEY = "r_o_loading_key_";

    //服务端返回 1
    public static int RESULT_SUCCESS = 1;

    //服务端返回 2
    public static int RESULT_FAILURE = 0;

    //缓存时间过期时间常量，标识没有过期时间的数据
    public static long ACTUAL_EXPIRE_TIME = -1L;

    //过期时间标识key
    public static String ACTUAL_EXPIRE_TIME_KEY = "actualExpireTimeKey";

    //返回ok
    public static String OK = "OK";

    //加载缓存过期时间key
    public static String LOADING_KEY_EXPIRE = "loadingKeyExpire";

    //缓存保存时间扩展时间Key
    public static String EXTEND_EXPIRE = "extendExpire";

    //失败重试次数
    public static String RETRY_TIME = "retryTime";

    //redis异常重试频率
    public static String RETRY_PERIOD = "retryPeriod";

    //线程阻塞时间
    public static String BLOCK_TIME = "blockTime";
}
