package cn.smallpotato.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author panjb
 */
public abstract class AbstractCrawler<T, E extends Element> implements Crawler<T, E> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractCrawler.class);

    private volatile boolean isCancel;

    protected final BlockingQueue<T> taskQueue;
    protected final CountDownLatch countDownLatch;
    protected final BlockingQueue<Element> elementQueue;

    public AbstractCrawler(BlockingQueue<T> taskQueue,
                           BlockingQueue<Element> elementQueue,
                           CountDownLatch countDownLatch) {
        this.taskQueue = taskQueue;
        this.elementQueue = elementQueue;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void cancel() {
        this.isCancel = true;
        if (logger.isInfoEnabled()) {
            logger.info("Crawler [{}] receives cancel signal", Thread.currentThread().getName());
        }
    }

    @Override
    public void run() {
        try {
            while (!isCancel && !taskQueue.isEmpty()) {
                T task = this.taskQueue.take();
                int cnt = 0;
                Iterable<E> elements = this.crawling(task);
                for (E element : elements) {
                    cnt++;
                    elementQueue.put(element);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Task [{}] crawling completed, data[{}]", task, cnt);
                }
            }
        } catch (InterruptedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Crawler [{}] is running abnormally", Thread.currentThread().getName(), e);
            }
        } finally {
            countDownLatch.countDown();
            if (logger.isInfoEnabled()) {
                logger.info("Crawler [{}] stops crawling data", Thread.currentThread().getName());
            }
        }
    }
}
