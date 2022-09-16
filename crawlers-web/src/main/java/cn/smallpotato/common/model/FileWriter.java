package cn.smallpotato.common.model;

import cn.smallpotato.output.Sink;

import java.util.concurrent.BlockingQueue;

/**
 * @author panjb
 */
public class FileWriter<T extends Element> extends AbstractWriter<T> {
    private final Sink<T> sink;

    public FileWriter(BlockingQueue<Element> queue, Sink<T> sink) {
        super(queue);
        this.sink = sink;
    }

    @Override
    public void write(T product) {
        sink.process(product);
    }

    @Override
    public void close() {
        this.sink.close();
    }
}
