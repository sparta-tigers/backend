package com.sparta.spartatigers.domain.liveboard.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.liveboard.dto.LiveBoardRoomResponseDto;
import com.sparta.spartatigers.domain.liveboard.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.liveboard.repository.LiveBoardConnectionRepository;
import com.sparta.spartatigers.domain.liveboard.repository.LiveBoardRoomRepository;
import com.sparta.spartatigers.domain.liveboard.model.Match;
import com.sparta.spartatigers.domain.liveboard.model.MatchResult;
import com.sparta.spartatigers.domain.liveboard.repository.MatchRepository;
import com.sparta.spartatigers.domain.liveboard.model.Stadium;
import com.sparta.spartatigers.domain.weather.dto.ForeCastResponseDto;
import com.sparta.spartatigers.domain.weather.dto.NowCastResponseDto;
import com.sparta.spartatigers.domain.weather.service.WeatherService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LiveboardRoomService {

	private final LiveBoardRoomRepository roomRepository;
	private final LiveBoardConnectionRepository connectionRepository;
	private final MatchRepository matchRepository;
	private final WeatherService weatherService;
	private final Clock clock;

	public String createRoomsForDay(LocalDate anyday) {
		// 1. 특정 날짜의 00:00 ~ 24:00 까지 선택해 경기 찾기
		String day = anyday.format(DateTimeFormatter.ofPattern("MM/dd"));
		LocalDateTime start = anyday.atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<Match> dayOfMatches = matchRepository.findAllByMatchTimeBetween(start, end);

		// 2. 오늘 경기가 없다면 return
		if(dayOfMatches.isEmpty()) {
			return "[LIVEBOARD/ROOM] " + day + " | NO_MATCHES";
		}

		// 3. 매치ID들을 모아서 room 레파지토리에 기존재하는지 찾기
		Set<Long> matchIds
			= dayOfMatches.stream().map(Match::getId).collect(Collectors.toSet());
		Set<Long> alreadyCreated
			= roomRepository.findAllByMatchIdIn(matchIds).stream().map(LiveBoardRoom::getMatchId).collect(Collectors.toSet());

		// 4. 룸 생성 로직
		int createdCount = 0;
		for(Match match : dayOfMatches) {
			// 4-1. 매치ID에 대해 룸이 이미 생성되어 있다면 SKIP
			if (alreadyCreated.contains(match.getId())) continue;
			// 4-2.룸이 존재하지 않고, 결과가 NOT_PLAYED일때만 생성
			if (MatchResult.NOT_PLAYED.equals(match.getMatchResult())) {
				createRoom(match);
				createdCount++;
			}
		}

		// 5. string으로 응답
		long totalRoomsToday = roomRepository.findAllByDate(anyday).size();
		long creatableCount = dayOfMatches.stream()
			.filter(match -> MatchResult.NOT_PLAYED.equals(match.getMatchResult()))
			.filter(m -> !alreadyCreated.contains(m.getId()))
			.count();

		if(createdCount == 0) {
			return "[LIVEBOARD/ROOM] " + day + " | ALREADY_CREATED" + " | TOTAL : " + totalRoomsToday;
		} else if (createdCount < creatableCount) {
			return "[LIVEBOARD/ROOM] " + day + " | PARTIALLY_CREATED : " + createdCount + " | TOTAL : " + totalRoomsToday;
		} else {
			return "[LIVEBOARD/ROOM] " + day + " | CREATED : " + createdCount + " | TOTAL : " + totalRoomsToday;
		}
	}

	public List<LiveBoardRoomResponseDto> getRoomsForDay(LocalDate anyday) {
		LocalDateTime start = anyday.atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<Match> dayOfMatches = matchRepository.findAllByMatchTimeBetween(start, end);

		Map<Long, LiveBoardRoom> roomMap = roomRepository.findAllByDate(anyday).stream()
			.collect(Collectors.toMap(LiveBoardRoom::getMatchId, Function.identity()));

		List<LiveBoardRoomResponseDto> roomDtosForDay =
			dayOfMatches.stream()
				.map(match -> {
				LiveBoardRoom room = roomMap.get(match.getId());

				if(room == null) {
					return LiveBoardRoomResponseDto.fromUpcomingMatch(match);
				}

				LocalDate matchDate = match.getMatchTime().toLocalDate();
				LocalDate realToday = LocalDateTime.now(clock).toLocalDate();

				if(matchDate.isEqual(realToday)) { // 당일 경기
					long connectCount = connectionRepository.getConnectionCount(room.getRoomId());

					Stadium stadium = match.getStadium();
					if (stadium == null) {
						log.warn("[ROOM] Stadium info missing for Match ID: {}. Skipping weather info.", match.getId());
						return LiveBoardRoomResponseDto.fromTodayMatch(match, room, connectCount, null, null);
					}

					NowCastResponseDto nowCast = weatherService.getNowCast(stadium.getId());
					List<ForeCastResponseDto> foreCast = weatherService.getForeCast(stadium.getId());
					return LiveBoardRoomResponseDto.fromTodayMatch(match, room, connectCount, nowCast, foreCast);
				} else if (matchDate.isBefore(realToday)) { // 지난 경기
					return LiveBoardRoomResponseDto.fromPastMatch(match, room);
				} else { // 그외의 예정 경기
					return LiveBoardRoomResponseDto.fromUpcomingMatch(match);
				}
			}).toList();

		return roomDtosForDay;
	}

	public String deleteRoomsForDay(LocalDate anyday) {
		// 1. 삭제할 날짜의 Room 선택
		String day = anyday.format(DateTimeFormatter.ofPattern("MM/dd"));
		List<LiveBoardRoom> roomsToDelete = roomRepository.findAllByDate(anyday);
		if(roomsToDelete.isEmpty()) {
			return "[LIVEBOARD/ROOM] " + day + " | NO_ROOMS_FOUND";
		}

		int deletedCount = 0;

		// 2. Room들의 MatchResult 확인을 위해 Match에 접근 필요, NOT PLAYED 확인후 삭제
		Set<Long> matchIds = roomsToDelete.stream().map(LiveBoardRoom::getMatchId).collect(Collectors.toSet());
		List<Match> matches = matchRepository.findAllByIdIn(matchIds);
		Map<Long, Match> matchMap = matches.stream().collect(Collectors.toMap(Match::getId, Function.identity()));

		for(LiveBoardRoom room : roomsToDelete) {
			Match match = matchMap.get(room.getMatchId());
			if(match == null) continue;

			if(MatchResult.NOT_PLAYED.equals(match.getMatchResult())) continue;

			roomRepository.deleteRoom(room.getRoomId());
			connectionRepository.deleteAllConnections(room.getRoomId());
			deletedCount ++;
		}
		long totalRoomsLeft = roomRepository.findAllByDate(anyday).size();

		if (deletedCount == 0) {
			return "[LIVEBOARD/ROOM] " + day + " | NO_DELETABLE_ROOMS | TOTAL : " + totalRoomsLeft;
		} else {
			return "[LIVEBOARD/ROOM] " + day + " | DELETED : " + deletedCount + " | TOTAL LEFT : " + totalRoomsLeft;
		}
	}


	// ✅분리 메서드 - 경기별로 룸 하나씩 생성
	private LiveBoardRoom createRoom(Match match) {
		String roomId = "LIVEBOARD_" + match.getId();
		String title = match.getAwayTeam().getName() + "VS" + match.getHomeTeam().getName();
		LocalDateTime matchTime = match.getMatchTime();
		LiveBoardRoom newRoom = LiveBoardRoom.of(roomId, match.getId(), title, matchTime);
		roomRepository.saveRoom(newRoom);
		return newRoom;
	}

	// ✅분리 메서드 - 룸 아이디별로 개별 삭제
	public String deleteRoom(String roomId) {
		LiveBoardRoom room = roomRepository.findRoomById(roomId);
		if (room == null) {
			return "ALREADY_DELETED";
		}
		roomRepository.deleteRoom(roomId);
		connectionRepository.deleteAllConnections(room.getRoomId());
		return "DELETED"; // TODO: 날짜별로 지워지게
	}
}
