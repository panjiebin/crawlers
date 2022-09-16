package cn.smallpotato.common.model;

/**
 * @author panjb
 */
public interface Writer<E extends Element> extends Runnable {

    void write(E ele);

    void close();
}
