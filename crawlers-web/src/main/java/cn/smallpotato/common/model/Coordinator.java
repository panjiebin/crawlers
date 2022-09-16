package cn.smallpotato.common.model;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * @author panjb
 */
public abstract class Coordinator<TASK, E extends Element> {

    private final static Logger logger = LoggerFactory.getLogger(Coordinator.class);

    protected final ExecutorService executor = new ThreadPoolExecutor(6,
            2 * Runtime.getRuntime().availableProcessors(),
            0,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("worker-%d").build());

    public final void start() {
        BlockingQueue<TASK> tasks = this.getCrawlerTasks();
        CountDownLatch countDownLatch = new CountDownLatch(crawlerSize());
        BlockingQueue<Element> queue = new LinkedBlockingQueue<>();
        for (int i = 0; i < crawlerSize(); i++) {
            executor.execute(createCrawler(tasks, queue, countDownLatch));
        }
        for (int i = 0; i < writerSize(); i++) {
            executor.execute(createWriter(queue));
        }
        executor.shutdown();
        try {
            countDownLatch.await();
            if (logger.isInfoEnabled()) {
                logger.info("All crawlers have stopped");
            }
            queue.put(Element.POISON_PILL);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * get crawler task
     * @return crawler task
     */
    protected abstract BlockingQueue<TASK> getCrawlerTasks();

    /**
     * create writer
     * @param queue crawler result
     * @return writer
     */
    protected abstract Writer<E> createWriter(BlockingQueue<Element> queue);

    /**
     * create crawler
     * @param taskQueue writer
     * @param elementQueue crawler result
     * @param countDownLatch latch
     * @return Crawler
     */
    protected abstract Crawler createCrawler(BlockingQueue<TASK> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch);

    protected int crawlerSize() {
        return 5;
    }

    protected int writerSize() {
        return 1;
    }
}
