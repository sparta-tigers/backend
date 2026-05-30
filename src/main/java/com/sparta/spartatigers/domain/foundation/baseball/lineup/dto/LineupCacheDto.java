package com.sparta.spartatigers.domain.foundation.baseball.lineup.dto;

import java.util.List;
import com.sparta.spartatigers.domain.foundation.baseball.lineup.model.LineupBatter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Redis 캐싱용 경량 라인업 DTO
 * 🚨 앙드레 카파시: 영속성 없이 메모리 내에서만 관리되는 휘발성 데이터 객체.
 * 무거운 전체 경기 데이터에서 라인업 정보만 추출하여 저장함으로써 메모리 효율성 극대화.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonIgnoreProperties(ignoreUnknown = true)
public class LineupCacheDto {
    private Long matchId;
    private List<LineupBatter> awayBatters;
    private List<LineupBatter> homeBatters;

    /**
     * 라인업 데이터가 유효한지(비어있지 않은지) 확인
     */
    @JsonIgnore
    public boolean isNotEmpty() {
        return (awayBatters != null && !awayBatters.isEmpty()) ||
                (homeBatters != null && !homeBatters.isEmpty());
    }
}
