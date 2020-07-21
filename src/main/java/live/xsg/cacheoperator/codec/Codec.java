package live.xsg.cacheoperator.codec;

/**
 * 编解码
 * Created by xsg on 2020/7/20.
 */
public interface Codec {

    Object encode(long expire, Object data);

    Object decode(Object data);
}
