package live.xsg.cacheoperator.resource;

import live.xsg.cacheoperator.loader.ResourceLoader;

/**
 * 资源注入
 * Created by xsg on 2020/8/10.
 */
public class DefaultResourceRegister implements ResourceRegister {

    //资源加载
    protected ResourceLoader resourceLoader;
    //资源
    protected Resource resource;

    public DefaultResourceRegister(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.resource = resourceLoader.getResource();
    }

    /**
     * 获取resource
     * @return resource
     */
    public Resource getResource() {
        return this.resource;
    }
}
