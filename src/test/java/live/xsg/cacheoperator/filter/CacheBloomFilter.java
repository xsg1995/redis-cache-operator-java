package live.xsg.cacheoperator.filter;

import com.google.common.base.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import live.xsg.cacheoperator.transport.Transporter;
import live.xsg.cacheoperator.transport.redis.RedisTransporter;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * 布隆过滤器
 * Created by xsg on 2020/8/4.
 */
public class CacheBloomFilter implements Filter {

    private Transporter transporter = new RedisTransporter();
    private Map<String, String> mockData;
    private BloomFilter<String> bloomFilter;

    public CacheBloomFilter() {
        this.init();
    }

    private void init() {

        mockData = DataContainer.getMockData();
        Set<String> keySet = mockData.keySet();
        long expectedInsertions = keySet.size();
        double fpp = 0.01;
        this.bloomFilter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), expectedInsertions, fpp);

        for (String key : keySet) {
            this.bloomFilter.put(key);
        }
    }

    @Override
    public boolean preFilter(String key) {
        boolean mightContain = this.bloomFilter.mightContain(key);
        if (!mightContain) {
            return false;
        }
        return true;
    }

    @Override
    public void postFilter(String key, Object result) {
        String hit = "hit";
        String notHit = "notHit";
        if (key != null) {
            if (result instanceof String) {
                String value = (String) result;
                if (StringUtils.isNotBlank(value)) {
                    this.transporter.incr(hit);
                } else {
                    this.transporter.incr(notHit);
                }
            } else {
                this.transporter.incr(notHit);
            }
        }
    }
}
