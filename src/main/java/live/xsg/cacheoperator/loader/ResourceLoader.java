package live.xsg.cacheoperator.loader;


import live.xsg.cacheoperator.resource.Resource;

/**
 * Resource加载接口
 * Created by xsg on 2020/6/12.
 */
public interface ResourceLoader {

    /**
     * 获取Resource
     * @return Resource
     */
    Resource getResource();

    /**
     * 根据path获取Resource
     * @param path 资源的标识
     * @return Resource
     */
    Resource getResource(String path);
}
