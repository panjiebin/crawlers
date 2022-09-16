package cn.smallpotato.config;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author panjb
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, HttpPoolConfig config) {
        return builder.customizers(restTemplateCustomizer(config)).build();
    }

    @Bean
    public RestTemplateCustomizer restTemplateCustomizer(HttpPoolConfig config){
        return restTemplate -> {
            HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
            //创建连接管理器 默认就支持https http
            PoolingHttpClientConnectionManager pM = new PoolingHttpClientConnectionManager();
            pM.setMaxTotal(config.getMaxTotal());
            //同路由并发数
            pM.setDefaultMaxPerRoute(config.getDefaultMaxPerRoute());
            httpClientBuilder.setConnectionManager(pM);
            HttpClient httpClient = httpClientBuilder.build();
            //创建HttpComponentsClientHttpRequestFactory
            HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
            //连接超时
            requestFactory.setConnectTimeout(config.getConnectTimeout());
            //数据读取超时时间
            requestFactory.setReadTimeout(config.getReadTimeout());
            //连接不够用的等待时间
            requestFactory.setConnectionRequestTimeout(config.getConnectionRequestTimeout());
            //设置请求工厂
            restTemplate.setRequestFactory(requestFactory);
            //用UTF-8 StringHttpMessageConverter替换默认StringHttpMessageConverter
            //可以解决请求回来的数据乱码问题
            List<HttpMessageConverter<?>> newMessageConverters = new ArrayList<>();
            for(HttpMessageConverter<?> converter : restTemplate.getMessageConverters()){
                if(converter instanceof StringHttpMessageConverter){
                    StringHttpMessageConverter messageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
                    newMessageConverters.add(messageConverter);
                }else {
                    newMessageConverters.add(converter);
                }
            }
            restTemplate.setMessageConverters(newMessageConverters);
        };
    }
}
