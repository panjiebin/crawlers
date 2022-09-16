package cn.smallpotato.common.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author panjb
 */
public class TextFileWriter<E extends Element> extends AbstractWriter<E> {

    private final static Logger logger = LoggerFactory.getLogger(TextFileWriter.class);
    private final BufferedWriter writer;
    private final Function<E, String> stringConverter;
    private long cnt = 0;
    private static final int LOG_BATCH = 1000;

    public TextFileWriter(BlockingQueue<Element> queue, String filePath) {
        this(queue, filePath, false, new ReflectConverter<>());
    }

    public TextFileWriter(BlockingQueue<Element> queue, String filePath, boolean append, Function<E, String> stringConverter) {
        super(queue);
        this.stringConverter = stringConverter;
        try {
            this.writer = new BufferedWriter(new FileWriter(filePath, append));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(E element) {
        try {
            String line = stringConverter.apply(element);
            writer.write(line);
            cnt++;
            if (cnt % LOG_BATCH == 0) {
                writer.flush();
                if (logger.isInfoEnabled()) {
                    logger.info("写入[{}]条数据，总共写入[{}]条数据", LOG_BATCH, cnt);
                }
            }
            writer.newLine();
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("数据写入异常[{}]", element);
            }
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
                if (logger.isInfoEnabled()) {
                    logger.info("总共写入[{}]条数据", cnt);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class ReflectConverter<T> implements Function<T, String> {

        @Override
        public String apply(T t) {
            Class<?> aClass = t.getClass();
            List<Field> fields = Arrays.stream(aClass.getDeclaredFields())
                    .sorted(Comparator.comparing(Field::getName)).collect(Collectors.toList());
            StringJoiner sj = new StringJoiner(",");
            try {
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object value = Optional.of(field.get(t)).orElse("");
                    sj.add(value.toString());
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return sj.toString();
        }
    }
}
