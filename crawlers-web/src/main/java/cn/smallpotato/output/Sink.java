package cn.smallpotato.output;

import java.io.Serializable;

/**
 * @author panjb
 */
public interface Sink<T> extends Serializable {

    void init();

    void process(T t);

    void process(Iterable<T> iterable);

    void close();
}
