package com.sparta.spartatigers.domain.liveboard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sparta.spartatigers.domain.liveboard.dto.LineupResponseDto;

import lombok.RequiredArgsConstructor;

/**
 * 라인업 조회 REST 컨트롤러
 *
 * Why: 프론트엔드 라이브보드 룸의 "선수 라인업" 탭에서 콜드 오픈 시
 * STOMP 구독 없이 Redis 캐시 데이터를 REST로 조회하기 위함.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/liveboard")
public class LineupController {

    private final LineupQueryService lineupQueryService;

    /**
     * GET /api/liveboard/{matchId}/lineup
     *
     * 인증된 사용자만 접근 가능 (Spring Security 기존 설정 활용).
     * Redis 캐시에 데이터가 없으면 빈 배열로 200 응답.
     */
    @GetMapping("/{matchId}/lineup")
    public ResponseEntity<LineupResponseDto> getMatchLineup(@PathVariable Long matchId) {
        LineupResponseDto response = lineupQueryService.getMatchLineup(matchId);
        return ResponseEntity.ok(response);
    }
}
