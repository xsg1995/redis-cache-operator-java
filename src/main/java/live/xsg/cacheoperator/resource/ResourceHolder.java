package live.xsg.cacheoperator.resource;

import live.xsg.cacheoperator.loader.PropertiesResourceLoader;
import live.xsg.cacheoperator.loader.ResourceLoader;

/**
 * 资源持有者
 * Created by xsg on 2020/8/7.
 */
public class ResourceHolder {

    private static ResourceHolder holder = new ResourceHolder();

    //资源加载器
    private ResourceLoader resourceLoader = new PropertiesResourceLoader();
    //资源
    private Resource resource;

    public static ResourceHolder getInstance() {
        return holder;
    }

    private ResourceHolder() {
        this.resource = this.resourceLoader.getResource();
    }

    public Resource getResource() {
        return this.resource;
    }
}
