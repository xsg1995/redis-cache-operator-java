package live.xsg.cacheoperator.support;

import java.util.Comparator;
import java.util.List;

/**
 * 排序order
 * Created by xsg on 2020/8/21.
 */
public class OrderComparator implements Comparator<Object> {

    public static final OrderComparator INSTANCE = new OrderComparator();

    @Override
    public int compare(Object o1, Object o2) {
        boolean b1 = o1 instanceof Order;
        boolean b2 = o2 instanceof Order;
        if (b1 && !b2) {
            return -1;
        } else if (b2 && !b1) {
            return 1;
        } else {
            int order1 = this.getOrder(o1);
            int order2 = this.getOrder(o2);
            return Integer.compare(order1, order2);
        }
    }

    private int getOrder(Object obj) {
        return obj instanceof Order ? ((Order) obj).getOrder() : Integer.MAX_VALUE;
    }

    public static void sort(List<?> list) {
        if (list != null && list.size() > 0) {
            list.sort(INSTANCE);
        }
    }

}
