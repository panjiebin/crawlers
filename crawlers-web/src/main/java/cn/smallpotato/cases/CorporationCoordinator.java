package cn.smallpotato.cases;

import cn.smallpotato.output.FileSink;
import cn.smallpotato.output.Sink;
import cn.smallpotato.utils.HttpUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.assertj.core.util.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author panjb
 */
public class CorporationCoordinator {

    private final static Logger logger = LoggerFactory.getLogger(CorporationCoordinator.class);

    private final ExecutorService executor = new ThreadPoolExecutor(6,
            10,
            0,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("crawler-%d").build());

    public static void main(String[] args) {
        new CorporationCoordinator().start();
    }
    public void start() {
        BlockingQueue<String> prefixQueue = new LinkedBlockingQueue<>(getAllPrefix());
        int threads = 5;
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        CountDownLatch countDownLatch = new CountDownLatch(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(new Crawler(prefixQueue, countDownLatch, queue));
        }
        executor.execute(new Writer(queue, "D:\\Corporation.txt"));
        executor.shutdown();
        try {
            countDownLatch.await();
            logger.info("公司名称爬取完毕！");
            queue.put("-1");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class Writer implements Runnable {

        private final BlockingQueue<String> queue;
        private final Sink<String> sink;

        public Writer(BlockingQueue<String> queue, String filePath) {
            this.queue = queue;
            this.sink = new FileSink<>(filePath, false, s -> s);
            sink.init();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    String s = queue.take();
                    if ("-1".equals(s)) {
                        logger.info("数据爬取完成");
                        break;
                    } else {
                        sink.process(s);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                this.sink.close();
            }
        }
    }

    private static class Crawler implements Runnable {

        private final ObjectMapper objectMapper = new ObjectMapper();
        private final CountDownLatch countDownLatch;
        private final BlockingQueue<String> prefixQueue;
        private final BlockingQueue<String> queue;

        public Crawler(BlockingQueue<String> prefixQueue, CountDownLatch countDownLatch, BlockingQueue<String> queue) {
            this.prefixQueue = prefixQueue;
            this.countDownLatch = countDownLatch;
            this.queue = queue;
        }

        @Override
        public void run() {
            String url = "https://apps.sfc.hk/publicregWeb/searchByRaJson?_dc=1663234581536";
            Map<String, String> headers = Maps.newHashMap("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
            Map<String, String> params = getParams();
            try {
                while (!prefixQueue.isEmpty()) {
                    String prefix = prefixQueue.take();
                    params.put("nameStartLetter", prefix);
                    Optional.of(HttpUtils.doPost(url, headers, params, String.class))
                            .ifPresent(content -> {
                                int cnt = 0;
                                try {
                                    JsonNode jsonNode = objectMapper.readTree(content);
                                    JsonNode items = jsonNode.get("items");
                                    if (items.isArray()) {
                                        for (JsonNode item : items) {
                                            queue.put(item.get("name").asText());
                                            cnt++;
                                        }
                                    }
                                    logger.info("prefix[{}], items[{}], total count[{}]", prefix, cnt, jsonNode.get("totalCount").asInt());
                                } catch (JsonProcessingException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                countDownLatch.countDown();
                logger.info("downloader[{}] stop", Thread.currentThread().getName());
            }
        }

        private Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>(8);
            params.put("licstatus", "active");
            params.put("ratype", "9");
            params.put("roleType", "corporation");
            params.put("page", "9");
            params.put("start", "0");
            params.put("nameStartLetter", "C");
            params.put("limit", "230");
            return params;
        }
    }

    private List<String> getAllPrefix() {
        List<String> prefix = new ArrayList<>();
        for (int i = '0'; i <= '9'; i++) {
            prefix.add(String.valueOf((char)i));
        }
        for (int i = 'A'; i <= 'Z'; i++) {
            prefix.add(String.valueOf((char)i));
        }
        return prefix;
    }
}
