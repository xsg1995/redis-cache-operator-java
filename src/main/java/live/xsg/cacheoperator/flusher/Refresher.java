package live.xsg.cacheoperator.flusher;

/**
 * 缓存刷新接口
 * Created by xsg on 2020/7/20.
 */
public interface Refresher<T> {

    T refresh();
}
