package live.xsg.cacheoperator.common;

/**
 * 系统常量
 * Created by xsg on 2020/7/21.
 */
public class Constants {

    //空字符串
    public static String EMPTY_STRING = "";

    //设置正在刷新缓存标识前置
    public static String LOADING_KEY = "r_o_loading_key_";

    //服务端返回 1
    public static int RESULT_SUCCESS = 1;

    //服务端返回 2
    public static int RESULT_FAILURE = 0;

    //缓存最大过期时间常量，用于标识作用
    public static long ABSOLUTE_EXPIRE_TIME = -1L;

    //返回ok
    public static String OK = "OK";
}
