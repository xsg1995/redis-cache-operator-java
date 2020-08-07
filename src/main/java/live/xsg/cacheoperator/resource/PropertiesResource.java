package live.xsg.cacheoperator.resource;

import java.util.Properties;

/**
 * 获取properties资源数据
 * Created by xsg on 2020/8/7.
 */
public class PropertiesResource implements Resource {

    private Properties properties;

    public PropertiesResource(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String getString(String key, String defaultVal) {
        try {
            return this.properties.getProperty(key);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    @Override
    public Integer getInt(String key, int defaultVal) {
        try {
            return Integer.parseInt(this.properties.getProperty(key));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    @Override
    public Long getLong(String key, long defaultVal) {
        try {
            return Long.parseLong(this.properties.getProperty(key));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return defaultVal;
    }

    @Override
    public void set(String key, String value) {
        this.properties.put(key, value);
    }
}
