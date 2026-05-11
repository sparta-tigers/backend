package com.sparta.spartatigers.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 기상청 API 전용 RestTemplate (타임아웃 설정 포함)
     *
     * Why: 기상청 API는 순간 지연이 잦아 타임아웃 없이 호출하면 DB 커넥션을 오래 점유하거나
     * 전체 요청이 무한 대기에 빠질 수 있다.
     * - connectTimeout: 3초 (DNS/TCP 연결 실패를 빠르게 감지)
     * - readTimeout: 5초 (응답 수신 대기 상한)
     * 기상청 평균 응답은 1~2초 이내이므로 5초는 충분한 여유다.
     */
    @Bean(name = "weatherRestTemplate")
    public RestTemplate weatherRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3_000);
        factory.setReadTimeout(5_000);
        return new RestTemplate(factory);
    }
}
