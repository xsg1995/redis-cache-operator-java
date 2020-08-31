package live.xsg.cacheoperator.codec.data;

/**
 * 编码类型数据父类
 * Created by xsg on 2020/8/31.
 */
public class AbstractCodecData {
    //缓存实际过期时间，单位：ms
    long actualExpireTime;

    /**
     * 判断缓存的时间过期时间是否过期
     * @return 返回true，则过期，返回false，则未过期
     */
    public boolean isInvalid() {
        return this.actualExpireTime <= System.currentTimeMillis();
    }
}
