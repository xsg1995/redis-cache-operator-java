package live.xsg.cacheoperator.core;

import live.xsg.cacheoperator.flusher.Refresher;
import live.xsg.cacheoperator.loader.ResourceLoader;
import live.xsg.cacheoperator.transport.Transporter;

import java.util.List;

/**
 * list类型操作实现
 * Created by xsg on 2020/8/28.
 */
public class RedisListOperator extends AbstractRedisOperator implements ListOperator {

    public RedisListOperator(Transporter transporter, ResourceLoader resourceLoader) {
        super(transporter, resourceLoader);
    }

    @Override
    public List<String> lrange(String key, long start, long end, long expire, Refresher<List<String>> flusher) {
        return null;
    }

    @Override
    public String lpop(String key, long expire, Refresher<List<String>> flusher) {
        return null;
    }

    @Override
    public String rpop(String key, long expire, Refresher<List<String>> flusher) {
        return null;
    }

    @Override
    protected Object getDataIgnoreValid(String key) {
        return null;
    }
}
