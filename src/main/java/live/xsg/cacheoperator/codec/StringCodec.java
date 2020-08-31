package live.xsg.cacheoperator.codec;


import com.alibaba.fastjson.JSONObject;
import live.xsg.cacheoperator.codec.data.StringData;
import live.xsg.cacheoperator.common.Constants;

/**
 * Created by xsg on 2020/7/20.
 */
public class StringCodec extends AbstractCodec {
    @Override
    public Object encode(long expire, Object message) {
        String data = (String) message;
        if (data == null) {
            data = "";
        }
        long actualExpireTime = this.getActualExpireTime(expire);
        StringData stringData = new StringData(actualExpireTime, data);
        return JSONObject.toJSONString(stringData);
    }

    @Override
    public Object decode(Object message) {
        String data = (String) message;
        if (data == null) {
            return new StringData(Constants.ACTUAL_EXPIRE_TIME, Constants.EMPTY_STRING);
        }

        StringData stringData;
        try {
            stringData = JSONObject.parseObject(data, StringData.class);
        } catch (Exception e) {
            //兼容非 encode 处理过的数据
            stringData = new StringData(Constants.ACTUAL_EXPIRE_TIME, data);
        }
        if (stringData == null) {
            stringData = new StringData(Constants.ACTUAL_EXPIRE_TIME, data);
        }
        return stringData;
    }

}


