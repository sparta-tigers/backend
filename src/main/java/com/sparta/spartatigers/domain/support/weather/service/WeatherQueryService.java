package com.sparta.spartatigers.domain.support.weather.service;

import com.sparta.spartatigers.domain.foundation.baseball.match.model.Match;
import com.sparta.spartatigers.domain.foundation.baseball.match.repository.MatchRepository;
import com.sparta.spartatigers.domain.foundation.baseball.team.model.Stadium;
import com.sparta.spartatigers.domain.support.weather.dto.MatchWeatherResponseDto;
import com.sparta.spartatigers.domain.support.weather.dto.WeatherBundle;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 라이브보드 룸 구장날씨 탭 전용 조회 서비스
 *
 * Why: 프론트엔드가 matchId만으로 NowCast + ForeCast를 단일 호출에 받도록
 * matchId → stadiumId 해석을 백엔드에서 처리한다.
 */
@Service
@RequiredArgsConstructor
public class WeatherQueryService {

    private final MatchRepository matchRepository;
    private final WeatherService weatherService;

    @Transactional(readOnly = true)
    public MatchWeatherResponseDto getMatchWeather(Long matchId) {
        Match match = matchRepository.findByMatchId(matchId)
                .orElseThrow(() -> new InvalidRequestException(ExceptionCode.MATCH_NOT_FOUND));

        Stadium stadium = match.getStadium();
        if (stadium == null) {
            throw new InvalidRequestException(ExceptionCode.STADIUM_NOT_FOUND);
        }

        WeatherBundle bundle = weatherService.getNowCastAndForeCast(stadium.getId());

        return MatchWeatherResponseDto.of(
            stadium.getName(),
            bundle.status(),
            bundle.nowCast(),
            bundle.foreCast()
        );
    }
}
