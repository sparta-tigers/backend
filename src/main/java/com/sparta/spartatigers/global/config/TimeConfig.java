package com.sparta.spartatigers.global.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 전역 시간 관리를 위한 설정 클래스
 *
 * Why: TimeZone.setDefault()는 JVM 전체의 상태를 변경하여 예측 불가능한 사이드 이펙트를 발생시킨다.
 * Clock 빈을 Asia/Seoul로 등록하여 시간 의존성이 있는 컴포넌트들이 이를 주입받아 사용하게 함으로써,
 * 테스트 가능하고 명시적인 시간 제어를 가능하게 한다.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("Asia/Seoul"));
    }
}
