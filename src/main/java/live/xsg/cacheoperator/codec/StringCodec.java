package live.xsg.cacheoperator.codec;


import com.alibaba.fastjson.JSONObject;
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

    public static class StringData {
        //失效日期
        long actualExpireTime;
        //实际数据
        String data;

        public StringData(long actualExpireTime, String data) {
            this.actualExpireTime = actualExpireTime;
            this.data = data;
        }

        public long getActualExpireTime() {
            return actualExpireTime;
        }

        public void setActualExpireTime(long actualExpireTime) {
            this.actualExpireTime = actualExpireTime;
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
                    "actualExpireTime=" + actualExpireTime +
                    ", data='" + data + '\'' +
                    '}';
        }
    }

    public static void main(String[] args) {
        StringCodec stringCodec = new StringCodec();
        String encode = (String) stringCodec.encode(1, null);
        System.out.println(encode);
    }

}


