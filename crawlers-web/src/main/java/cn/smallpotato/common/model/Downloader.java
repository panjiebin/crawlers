package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface Downloader<T, E extends Element> extends Runnable {

    Iterable<E> download(T task);

    void cancel();
}
