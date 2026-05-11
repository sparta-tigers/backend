package com.sparta.spartatigers;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import jakarta.annotation.PostConstruct;

@ConfigurationPropertiesScan
@EnableScheduling
@SpringBootApplication
public class SpartatigersApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpartatigersApiApplication.class, args);
    }

    /**
     * 애플리케이션 기본 타임존을 KST로 고정.
     *
     * Why: 기상청 API는 base_date/base_time을 KST 문자열로 요구하고,
     * LiveBoard/Match 스케줄도 KST 기준이다. 컨테이너/호스트가 UTC일 경우
     * LocalDateTime.now()가 KST-9h로 계산되어 구장날씨 탭의 데이터가
     * 9시간 전 발표본으로 잘못 매핑되는 문제가 있어 진입점에서 강제한다.
     */
    @PostConstruct
    public void setDefaultTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

}
