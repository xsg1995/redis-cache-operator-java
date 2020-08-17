package live.xsg.cacheoperator.codec;

/**
 * Codec的抽象父类
 * Created by xsg on 2020/8/17.
 */
public abstract class AbstractCodec implements Codec {

    protected long getAbsolutionExpireTime(long expire) {
        return System.currentTimeMillis() + expire;
    }
}
