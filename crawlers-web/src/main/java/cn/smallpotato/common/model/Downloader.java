package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface Downloader<T, E extends Element> extends Runnable {

    Iterable<E> crawling(T task);

    void cancel();
}
