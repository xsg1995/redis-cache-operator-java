package live.xsg.cacheoperator.codec.data;

import java.util.Map;

/**
 * map 编码数据
 * Created by xsg on 2020/8/31.
 */
public class MapData extends AbstractCodecData {
    //实际数据
    private Map<String, String> data;

    public MapData(long actualExpireTime, Map<String, String> data) {
        this.actualExpireTime = actualExpireTime;
        this.data = data;
    }

    public long getActualExpireTime() {
        return actualExpireTime;
    }

    public Map<String, String> getData() {
        return data;
    }
}
