package cn.smallpotato.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

/**
 * @author panjb
 */
public abstract class AbstractWriter<T extends Element> implements Writer<T> {

    private final static Logger logger = LoggerFactory.getLogger(AbstractWriter.class);

    private final BlockingQueue<Element> queue;

    public AbstractWriter(BlockingQueue<Element> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Element element = queue.take();
                if (element == Element.POISON_PILL) {
                    queue.put(Element.POISON_PILL);
                    if (logger.isInfoEnabled()) {
                        logger.info("Writer [{}] stops write", Thread.currentThread().getName());
                    }
                    break;
                } else {
                    this.write((T) element);
                }
            }
        } catch (InterruptedException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Writer [{}] is running abnormally", Thread.currentThread().getName(), e);
            }
        } finally {
            this.close();
        }
    }
}
