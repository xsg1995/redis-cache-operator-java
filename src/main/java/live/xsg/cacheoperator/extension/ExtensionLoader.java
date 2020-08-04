package live.xsg.cacheoperator.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * 扩展类加载
 * Created by xsg on 2020/8/4.
 */
public class ExtensionLoader<T> {

    /**
     * 使用java SPI，根据Class加载扩展点实现
     * @param clazz 要获取的扩展类的类型
     * @return 返回扩展类的实现
     */
    public List<T> getExtensions(Class<T> clazz) {
        List<T> res = new ArrayList<>();
        ServiceLoader<T> loader = ServiceLoader.load(clazz);
        for (T t : loader) {
            res.add(t);
        }
        return res;
    }
}
