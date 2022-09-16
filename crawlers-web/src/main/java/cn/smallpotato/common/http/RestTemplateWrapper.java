package cn.smallpotato.common.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author panjb
 */
@Component
public class RestTemplateWrapper {

    private final RestTemplate restTemplate;

    public RestTemplateWrapper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public <T> T get(String url, Map<String, String> headers, ParameterizedTypeReference<T> type) {
        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpHeaders.set(entry.getKey(), entry.getValue());
            }
            HttpEntity<T> httpEntity = new HttpEntity<>(null, httpHeaders);
            ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, httpEntity, type);
            if(!response.getStatusCode().is2xxSuccessful()){
                throw new RuntimeException("Request={" + url + "}, code={" + response.getStatusCode() + "}, message={" + response.getBody() + "}");
            }
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
