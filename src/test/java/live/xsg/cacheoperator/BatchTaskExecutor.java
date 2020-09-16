package live.xsg.cacheoperator;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 批量任务运行
 * Created by xsg on 2020/9/16.
 */
public class BatchTaskExecutor {

    /**
     * 启动多个线程运行任务
     * @param nThread 线程数
     * @param run 具体任务
     */
    public void batchRun(int nThread, Runnable run) {
        if (nThread <= 0) return;

        try {
            CountDownLatch countDownLatch = new CountDownLatch(nThread);
            ExecutorService executorService = Executors.newFixedThreadPool(nThread);

            for (int i = 0; i < nThread; i++) {
                executorService.submit(() -> {
                    try {
                        run.run();
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
            executorService.shutdown();
        } catch (Exception ignored){}
    }
}
