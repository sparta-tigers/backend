package com.sparta.spartatigers.domain.liveboard;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sparta.spartatigers.domain.liveboard.dto.MatchWeatherResponse;
import com.sparta.spartatigers.domain.liveboard.match.model.Match;
import com.sparta.spartatigers.domain.liveboard.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.liveboard.match.model.Stadium;
import com.sparta.spartatigers.domain.weather.dto.WeatherBundle;
import com.sparta.spartatigers.domain.weather.service.WeatherService;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import lombok.RequiredArgsConstructor;

/**
 * 라이브보드 룸 구장날씨 탭 전용 조회 서비스
 *
 * Why: 프론트엔드가 matchId만으로 NowCast + ForeCast를 단일 호출에 받도록
 * matchId → stadiumId 해석을 백엔드에서 처리한다.
 * - DB는 MatchRepository.findByMatchId 단일 호출
 * - 외부 API(기상청) 호출은 WeatherService에 위임
 */
@Service
@RequiredArgsConstructor
public class WeatherQueryService {

    private final MatchRepository matchRepository;
    private final WeatherService weatherService;

    @Transactional(readOnly = true)
    public MatchWeatherResponse getMatchWeather(Long matchId) {
        Match match = matchRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));

        Stadium stadium = match.getStadium();
        if (stadium == null) {
            throw new InvalidRequestException(ExceptionCode.STADIUM_NOT_FOUND);
        }

        // 기상청 API 3회 호출(NCST / UltraFcst / VilageFcst)을 단일 진입점에서 처리.
        // 기존 getNowCast + getForeCast 분리 호출 시 VilageFcst가 2회 중복되던 문제를 해소.
        WeatherBundle bundle = weatherService.getNowCastAndForeCast(stadium.getId());

        return MatchWeatherResponse.of(stadium.getName(), bundle.status(), bundle.nowCast(), bundle.foreCast());
    }
}
