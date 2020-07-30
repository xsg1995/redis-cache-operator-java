package live.xsg.cacheoperator.codec;


import com.alibaba.fastjson.JSONObject;
import live.xsg.cacheoperator.common.Constants;

/**
 * Created by xsg on 2020/7/20.
 */
public class StringCodec implements Codec {
    @Override
    public Object encode(long expire, Object message) {
        String data = (String) message;
        long absoluteExpireTime = System.currentTimeMillis() + expire;
        StringData stringData = new StringData(absoluteExpireTime, data);
        return JSONObject.toJSONString(stringData);
    }

    @Override
    public Object decode(Object message) {
        String data = (String) message;
        StringData stringData;
        try {
            stringData = JSONObject.parseObject(data, StringData.class);
        } catch (Exception e) {
            //兼容非 encode 处理过的数据
            stringData = new StringData(Constants.ABSOLUTE_EXPIRE_TIME, data);
        }
        if (stringData == null) {
            stringData = new StringData(Constants.ABSOLUTE_EXPIRE_TIME, Constants.EMPTY_STRING);
        }
        return stringData;
    }

    public static class StringData {
        //失效日期
        long absoluteExpireTime;
        //实际数据
        String data;

        public StringData() {
        }

        public StringData(long absoluteExpireTime, String data) {
            this.absoluteExpireTime = absoluteExpireTime;
            this.data = data;
        }

        public long getAbsoluteExpireTime() {
            return absoluteExpireTime;
        }

        public void setAbsoluteExpireTime(long absoluteExpireTime) {
            this.absoluteExpireTime = absoluteExpireTime;
        }

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        @Override
        public String toString() {
            return "StringData{" +
                    "absoluteExpireTime=" + absoluteExpireTime +
                    ", data='" + data + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        String message = "message";
        StringData stringData = JSONObject.parseObject(message, StringData.class);
        System.out.println(stringData);
    }

}


