package cn.smallpotato.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author panjb
 */
public abstract class AbstractWithInputCrawler<IN, OUT extends Element>
        implements WithInputCrawler<IN, OUT> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractWithInputCrawler.class);

    private volatile boolean isCancel;

    protected final BlockingQueue<IN> taskQueue;
    protected final CountDownLatch countDownLatch;
    protected final BlockingQueue<Element> elementQueue;

    public AbstractWithInputCrawler(BlockingQueue<IN> taskQueue,
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
                IN task = this.taskQueue.take();
                int cnt = 0;
                Iterable<OUT> elements = this.crawling(task);
                for (OUT element : elements) {
                    cnt++;
                    elementQueue.put(element);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Task [{}] processing completed, data[{}]", task, cnt);
                }
            }
        } catch (InterruptedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Crawler [{}] is running abnormally", Thread.currentThread().getName(), e);
            }
        } finally {
            countDownLatch.countDown();
            if (logger.isInfoEnabled()) {
                logger.info("Crawler [{}] stops consuming", Thread.currentThread().getName());
            }
        }
    }
}
