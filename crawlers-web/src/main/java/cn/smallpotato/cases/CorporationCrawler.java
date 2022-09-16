package cn.smallpotato.cases;

import cn.smallpotato.common.http.HttpHelper;
import cn.smallpotato.common.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.util.Maps;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author panjb
 */
public class CorporationCrawler extends AbstractCrawler<String, Element.StringElement> {

    public static void main(String[] args) {
        new CorporationCrawler().start();
    }

    @Override
    protected BlockingQueue<String> getCrawlerTasks() {
        return new LinkedBlockingQueue<>(getAllPrefix());
    }

    @Override
    protected Writer<Element.StringElement> createWriter(BlockingQueue<Element> queue) {
        return new TextFileWriter<>(queue, "D:\\Corporation.csv");
    }

    @Override
    protected Downloader<String, Element.StringElement> createCrawler(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch) {
        return new CorporationDownloader(taskQueue, elementQueue, countDownLatch);
    }

    private static class CorporationDownloader extends AbstractDownloader<String, Element.StringElement> {

        private final ObjectMapper objectMapper = new ObjectMapper();

        private final Map<String, String> headers = Maps.newHashMap("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
        private final Map<String, String> params;

        public CorporationDownloader(BlockingQueue<String> taskQueue, BlockingQueue<Element> elementQueue, CountDownLatch countDownLatch) {
            super(taskQueue, elementQueue, countDownLatch);
            this.params = getParams();
        }

        @Override
        public Iterable<Element.StringElement> crawling(String prefix) {
            String url = "https://apps.sfc.hk/publicregWeb/searchByRaJson?_dc=1663234581536";
            params.put("nameStartLetter", prefix);
            List<Element.StringElement> list = new ArrayList<>();
            Optional.of(HttpHelper.doPost(url, headers, params, String.class))
                    .ifPresent(content -> {
                        try {
                            JsonNode jsonNode = objectMapper.readTree(content);
                            JsonNode items = jsonNode.get("items");
                            if (items.isArray()) {
                                for (JsonNode item : items) {
                                    list.add(new Element.StringElement(item.get("name").asText()));
                                }
                            }
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                    });
            return list;
        }

        private Map<String, String> getParams() {
            Map<String, String> params = new HashMap<>(8);
            params.put("licstatus", "active");
            params.put("ratype", "9");
            params.put("roleType", "corporation");
            params.put("page", "1");
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
