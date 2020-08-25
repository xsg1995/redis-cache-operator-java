package live.xsg.cacheoperator.codec;

import live.xsg.cacheoperator.common.Constants;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xsg on 2020/8/17.
 */
public class MapCodec extends AbstractCodec {

    @SuppressWarnings("unchecked")
    @Override
    public Object encode(long expire, Object message) {
        HashMap<String, String> inData = (HashMap<String, String>) message;
        HashMap<String, String> data;
        if (inData == null) {
            data = new HashMap<>();
        } else {
            data = (HashMap<String, String>) inData.clone();
        }

        long actualExpireTime = this.getActualExpireTime(expire);
        data.put(Constants.ACTUAL_EXPIRE_TIME_KEY, String.valueOf(actualExpireTime));

        return new MapData(actualExpireTime, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object decode(Object message) {
        Map<String, String> data = (Map<String, String>) message;
        if (data == null) {
            data = new HashMap<>();
        }

        String actualExpireTime = data.get(Constants.ACTUAL_EXPIRE_TIME_KEY);
        long targetActualExpireTime = StringUtils.isNotBlank(actualExpireTime) ? Long.parseLong(actualExpireTime) : Constants.ACTUAL_EXPIRE_TIME;
        data.remove(Constants.ACTUAL_EXPIRE_TIME_KEY);

        return new MapData(targetActualExpireTime, data);
    }

    public static class MapData {
        //失效日期
        long actualExpireTime;
        //实际数据
        Map<String, String> data;

        public MapData(long actualExpireTime, Map<String, String> data) {
            this.actualExpireTime = actualExpireTime;
            this.data = data;
        }

        public long getActualExpireTime() {
            return actualExpireTime;
        }

        public void setActualExpireTime(long actualExpireTime) {
            this.actualExpireTime = actualExpireTime;
        }

        public Map<String, String> getData() {
            return data;
        }

        public void setData(Map<String, String> data) {
            this.data = data;
        }
    }

}
