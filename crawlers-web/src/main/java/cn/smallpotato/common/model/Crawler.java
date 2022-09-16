package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface Crawler<T, E extends Element> extends Runnable {

    Iterable<E> crawling(T task);

    void cancel();
}
