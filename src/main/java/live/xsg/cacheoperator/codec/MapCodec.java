package live.xsg.cacheoperator.codec;

import live.xsg.cacheoperator.codec.data.MapData;
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
            return null;
        }

        String actualExpireTime = data.get(Constants.ACTUAL_EXPIRE_TIME_KEY);
        long targetActualExpireTime = StringUtils.isNotBlank(actualExpireTime) ? Long.parseLong(actualExpireTime) : Constants.ACTUAL_EXPIRE_TIME;
        data.remove(Constants.ACTUAL_EXPIRE_TIME_KEY);

        return new MapData(targetActualExpireTime, data);
    }

}
