package cn.smallpotato.cases;

import cn.smallpotato.output.FileSink;
import cn.smallpotato.output.Sink;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author panjb
 */
public class CorporationCoordinator {

    private final static Logger logger = LoggerFactory.getLogger(CorporationCoordinator.class);

    private final ExecutorService executor = new ThreadPoolExecutor(6,
            10,
            0,
            TimeUnit.MINUTES, new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder().setNameFormat("crawler-thread-%d").build());

    public static void main(String[] args) throws Exception {
        
        new CorporationCoordinator().start();
    }
    public void start() throws Exception {
        String url = "https://apps.sfc.hk/publicregWeb/searchByRaJson?_dc=1663234581536";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        httpPost.addHeader("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
        Map<String, NameValuePair> params = new HashMap<>();
        params.put("licstatus", new BasicNameValuePair("licstatus", "active"));
        params.put("ratype", new BasicNameValuePair("ratype", "9"));
        params.put("roleType", new BasicNameValuePair("roleType", "corporation"));
        params.put("page", new BasicNameValuePair("page", "9"));
        params.put("start", new BasicNameValuePair("start", "0"));
        params.put("nameStartLetter", new BasicNameValuePair("nameStartLetter", "C"));
        params.put("limit", new BasicNameValuePair("limit", "1"));
        List<String> prefixes = getAllPrefix();
        ObjectMapper objectMapper = new ObjectMapper();
        Sink<String> sink = new FileSink<>("D:\\Corporation.txt", true, s -> s);
        sink.init();
        for (String prefix : prefixes) {
            params.put("nameStartLetter", new BasicNameValuePair("nameStartLetter", prefix));
            httpPost.setEntity(new UrlEncodedFormEntity(new ArrayList<>(params.values()),"UTF-8"));
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // 响应的结果
                String content = EntityUtils.toString(entity, "UTF-8");
                JsonNode jsonNode = objectMapper.readTree(content);
                JsonNode items = jsonNode.get("items");
                if (items.isArray()) {
                    for (JsonNode item : items) {
                        JsonNode name = item.get("name");
                        sink.process(name.asText());
                    }
                }
            }
            response.close();
            Thread.sleep(RandomUtils.nextInt(1000, 3000));
            logger.info("prefix[{}] is ok", prefix);
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
