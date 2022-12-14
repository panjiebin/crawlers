package cn.smallpotato.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * @author panjb
 */
public abstract class AbstractDownloader<T, E extends Element> implements Downloader<T, E> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDownloader.class);

    private volatile boolean isCancel;

    protected final BlockingQueue<T> taskQueue;
    protected final CountDownLatch countDownLatch;
    protected final BlockingQueue<Element> elementQueue;

    public AbstractDownloader(BlockingQueue<T> taskQueue,
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
                Iterable<E> elements = this.download(task);
                for (E element : elements) {
                    cnt++;
                    elementQueue.put(element);
                }
                if (logger.isInfoEnabled()) {
                    logger.info("Task [{}] download completed, data[{}]", task, cnt);
                }
            }
        } catch (InterruptedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Downloader [{}] is running abnormally", Thread.currentThread().getName(), e);
            }
        } finally {
            countDownLatch.countDown();
            if (logger.isInfoEnabled()) {
                logger.info("Downloader [{}] stops download data", Thread.currentThread().getName());
            }
        }
    }
}
