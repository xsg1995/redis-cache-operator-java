package live.xsg.cacheoperator.loader;

import live.xsg.cacheoperator.exception.LoadResourceException;
import live.xsg.cacheoperator.resource.PropertiesResource;
import live.xsg.cacheoperator.resource.Resource;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.util.Properties;

/**
 * properties资源文件加载
 * Created by xsg on 2020/8/7.
 */
public class PropertiesResourceLoader implements ResourceLoader {

    //默认properties文件名称
    private static final String DEFAULT_PROPERTIES_FILE_NAME = "redis-cache-operator.properties";

    @Override
    public Resource getResource() {
        return this.getResource(DEFAULT_PROPERTIES_FILE_NAME);
    }

    @Override
    public Resource getResource(String path) {
        if (StringUtils.isBlank(path)) throw new IllegalArgumentException("path is null");

        PropertiesResource resource = null;
        try(InputStream in = PropertiesResourceLoader.class.getClassLoader().getResourceAsStream(path)) {
            Properties properties = new Properties();
            properties.load(in);
            resource = new PropertiesResource(properties);
        } catch (Exception e) {
            throw new LoadResourceException("properties文件规则读取异常", e);
        }
        return resource;
    }
}
