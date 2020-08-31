package live.xsg.cacheoperator.codec.data;

/**
 * String 编码数据
 * Created by xsg on 2020/8/31.
 */
public class StringData extends AbstractCodecData {
    //实际数据
    private String data;

    public StringData(long actualExpireTime, String data) {
        this.actualExpireTime = actualExpireTime;
        this.data = data;
    }

    public long getActualExpireTime() {
        return actualExpireTime;
    }

    public String getData() {
        return data;
    }
}
