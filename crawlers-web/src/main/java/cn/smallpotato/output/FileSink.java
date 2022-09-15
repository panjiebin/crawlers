package cn.smallpotato.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author panjb
 */
public class FileSink<T> implements Sink<T> {

    private final static Logger logger = LoggerFactory.getLogger(FileSink.class);

    private final String filePath;
    private final boolean append;
    private BufferedWriter writer;
    private final Function<T, String> converter;
    private long cnt = 0;
    private static final int LOG_CNT = 100;

    public FileSink(String filePath) {
        this(filePath, true, new ReflectConverter<>());
    }

    public FileSink(String filePath, boolean append, Function<T, String> converter) {
        this.filePath = filePath;
        this.append = append;
        this.converter = converter;
    }

    @Override
    public void init() {
        try {
            this.writer = new BufferedWriter(new FileWriter(filePath, append));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void process(T t) {
        try {
            String line = converter.apply(t);
            writer.write(line);
            cnt++;
            if (cnt % LOG_CNT == 0) {
                writer.flush();
                if (logger.isInfoEnabled()) {
                    logger.info("写入[{}]条数据，总共写入[{}]条数据", LOG_CNT, cnt);
                }
            }
            writer.newLine();
        } catch (IOException e) {
            if (logger.isWarnEnabled()) {
                logger.warn("数据写入异常[{}]", t);
            }
        }
    }

    @Override
    public void process(Iterable<T> iterable) {
        for (T t : iterable) {
            this.process(t);
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
