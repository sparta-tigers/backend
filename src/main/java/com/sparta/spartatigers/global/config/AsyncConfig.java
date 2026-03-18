package com.sparta.spartatigers.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리를 위한 Spring 설정 클래스
 * 
 * @EnableAsync 어노테이션을 통해 애플리케이션 전역에서
 * @Async 어노테이션을 사용한 비동기 처리를 활성화합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig {
    // 필요 시 ThreadPoolTaskExecutor Bean 등록 가능
}
