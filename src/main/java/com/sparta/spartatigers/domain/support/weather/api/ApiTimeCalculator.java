package com.sparta.spartatigers.domain.support.weather.api;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 기상청 발표 시각 계산기
 *
 * Why: 기상청 단기예보 API는 API 종류마다 발표 주기 · 데이터 제공 지연이 달라
 * 단순한 "현재 시각 - 1시간"으로는 No Data(Error 03)를 자주 맞게 된다.
 * 각 API의 실제 운영 스펙을 상수로 명시해 호출 실패를 줄인다.
 * Clock을 주입받아 테스트 가능하고 명시적인 시간 제어를 수행한다.
 */
@Component
@RequiredArgsConstructor
public class ApiTimeCalculator {

    private final Clock clock;

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMAT =
        DateTimeFormatter.ofPattern("HHmm");

    /** 초단기실황 제공 지연 (정시 발표 → +40분 이후부터 응답) */
    private static final int NCST_PROVISION_DELAY_MINUTES = 40;
    /** 초단기예보 제공 지연 (HH:30 발표 → +15분 이후부터 응답) */
    private static final int ULTRA_FCST_PROVISION_DELAY_MINUTES = 15;
    /** 단기예보 제공 지연 (발표 시각 → +30분 이후부터 응답, 운영 진동 대비) */
    private static final int VILAGE_FCST_PROVISION_DELAY_MINUTES = 30;

    /** 단기예보 발표 시각 룩업 (KST, 3시간 간격 총 8회/일) */
    private static final int[] VILAGE_BASE_HOURS = {
        2,
        5,
        8,
        11,
        14,
        17,
        20,
        23,
    };

    public record BaseDateTime(String baseDate, String baseTime) {}

    /**
     * 초단기실황 base_date/base_time
     */
    public BaseDateTime getNcstBaseDateTime() {
        LocalDateTime base = LocalDateTime.now(clock)
            .minusMinutes(NCST_PROVISION_DELAY_MINUTES)
            .truncatedTo(ChronoUnit.HOURS);

        return new BaseDateTime(
            base.format(DATE_FORMAT),
            base.format(TIME_FORMAT)
        );
    }

    /**
     * 초단기예보 base_date/base_time
     */
    public BaseDateTime getFcstBaseDateTime() {
        LocalDateTime adjusted = LocalDateTime.now(clock).minusMinutes(
            ULTRA_FCST_PROVISION_DELAY_MINUTES
        );
        LocalDateTime base;

        if (adjusted.getMinute() < 30) {
            base = adjusted
                .minusHours(1)
                .withMinute(30)
                .truncatedTo(ChronoUnit.MINUTES);
        } else {
            base = adjusted.withMinute(30).truncatedTo(ChronoUnit.MINUTES);
        }

        return new BaseDateTime(
            base.format(DATE_FORMAT),
            base.format(TIME_FORMAT)
        );
    }

    /**
     * 단기예보 base_date/base_time
     */
    public BaseDateTime getVilageFcstBaseDateTime() {
        LocalDateTime adjusted = LocalDateTime.now(clock).minusMinutes(
            VILAGE_FCST_PROVISION_DELAY_MINUTES
        );

        for (int i = VILAGE_BASE_HOURS.length - 1; i >= 0; i--) {
            if (adjusted.getHour() >= VILAGE_BASE_HOURS[i]) {
                LocalDateTime base = adjusted
                    .withHour(VILAGE_BASE_HOURS[i])
                    .withMinute(0)
                    .truncatedTo(ChronoUnit.HOURS);
                return new BaseDateTime(
                    base.format(DATE_FORMAT),
                    base.format(TIME_FORMAT)
                );
            }
        }

        LocalDateTime base = adjusted
            .minusDays(1)
            .withHour(23)
            .truncatedTo(ChronoUnit.HOURS);
        return new BaseDateTime(
            base.format(DATE_FORMAT),
            base.format(TIME_FORMAT)
        );
    }
}
