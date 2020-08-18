package live.xsg.cacheoperator.codec;

import live.xsg.cacheoperator.common.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xsg on 2020/8/17.
 */
public class MapCodec extends AbstractCodec {
    @Override
    public Object encode(long expire, Object message) {
        HashMap<String, String> inData = (HashMap<String, String>) message;
        HashMap<String, String> data = null;
        if (inData == null) {
            data = new HashMap<>();
        } else {
            data = (HashMap<String, String>) inData.clone();
        }

        long absoluteExpireTime = this.getAbsolutionExpireTime(expire);
        data.put(Constants.ABSOLUTE_EXPIRE_TIME_KEY, String.valueOf(absoluteExpireTime));

        return new MapData(absoluteExpireTime, data);
    }

    @Override
    public Object decode(Object message) {
        Map<String, String> data = (Map<String, String>) message;
        if (data == null) {
            data = new HashMap<>();
        }

        String absoluteExpireTime = data.get(Constants.ABSOLUTE_EXPIRE_TIME_KEY);
        long targetAbsoluteExpireTime = StringUtils.isNotBlank(absoluteExpireTime) ? Long.parseLong(absoluteExpireTime) : Constants.ABSOLUTE_EXPIRE_TIME;
        data.remove(Constants.ABSOLUTE_EXPIRE_TIME_KEY);

        return new MapData(targetAbsoluteExpireTime, data);
    }

    public static class MapData {
        //失效日期
        long absoluteExpireTime;
        //实际数据
        Map<String, String> data;

        public MapData(long absoluteExpireTime, Map<String, String> data) {
            this.absoluteExpireTime = absoluteExpireTime;
            this.data = data;
        }

        public long getAbsoluteExpireTime() {
            return absoluteExpireTime;
        }

        public void setAbsoluteExpireTime(long absoluteExpireTime) {
            this.absoluteExpireTime = absoluteExpireTime;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }

}
