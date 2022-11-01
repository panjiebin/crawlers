package cn.smallpotato.common.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author panjb
 */
public class HttpHelper {

    private static final int STATUS_CODE_OK = 200;
    private static final PoolingHttpClientConnectionManager CM;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        CM = new PoolingHttpClientConnectionManager();
        // 设置最大连接数
        CM.setMaxTotal(100);
        // 设置每个主机的最大连接数
        CM.setDefaultMaxPerRoute(10);
    }

    private HttpHelper() {
    }

    public static void downloadFile(String url, String filePath) {
        CloseableHttpClient client = getClient();
        HttpGet httpGet = new HttpGet(url);
        FileOutputStream os = null;
        InputStream is = null;
        try {
            CloseableHttpResponse response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            File file = new File(filePath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            os = new FileOutputStream(file);
            is = entity.getContent();
            byte[] buffer = new byte[1024];
            int ch;
            while ((ch = is.read(buffer)) != -1) {
                os.write(buffer, 0 ,ch);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> T doGet(String url, TypeReference<T> type) {
        return doGet(url, Collections.emptyMap(), type);
    }

    public static <T> T doGet(String url, Map<String, String> headers, TypeReference<T> type) {
        CloseableHttpClient httpClient = getClient();
        HttpGet httpGet = new HttpGet(url);
        setHeaders(headers, httpGet);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpGet);
            return parseResult(response, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T> T doPost(String url, TypeReference<T> type) {
        return doPost(url, Collections.emptyMap(), Collections.emptyMap(), type);
    }

    public static <T> T doPost(String url, Map<String, String> headers, TypeReference<T> type) {
        return doPost(url, headers, Collections.emptyMap(), type);
    }

    public static <T> T doPost(String url, TypeReference<T> type, Map<String, String> params) {
        return doPost(url, Collections.emptyMap(), params, type);
    }

    public static <T> T doPost(String url, Map<String, String> headers, Map<String, String> params, TypeReference<T> type) {
        CloseableHttpClient httpClient = getClient();
        HttpPost httpPost = new HttpPost(url);
        setHeaders(headers, httpPost);
        CloseableHttpResponse response = null;
        try {
            wrapParams(params, httpPost);
            response = httpClient.execute(httpPost);
            return parseResult(response, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (response != null) {
                    response.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static <T> T parseResult(CloseableHttpResponse response, TypeReference<T> type) throws IOException {
        if (response.getStatusLine().getStatusCode() == STATUS_CODE_OK) {
            if (response.getEntity() != null) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                return OBJECT_MAPPER.readValue(content, type);
            }
        }
        return null;
    }

    private static void setHeaders(Map<String, String> headers, HttpRequestBase request) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            request.addHeader(entry.getKey(), entry.getValue());
        }
    }

    private static void wrapParams(Map<String, String> params, HttpPost httpPost) throws UnsupportedEncodingException {
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, "UTF-8"));

    }

    public static CloseableHttpClient getClient() {
        return HttpClients.custom().setConnectionManager(CM).build();
    }
}
