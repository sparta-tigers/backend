package com.sparta.spartatigers.domain.liveboardroom.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.liveboardroom.dto.LiveBoardRoomResponseDto;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardStatus;
import com.sparta.spartatigers.domain.liveboardroom.repository.LiveBoardConnectionRepository;
import com.sparta.spartatigers.domain.liveboardroom.repository.LiveBoardRoomRepository;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;

import jakarta.persistence.ManyToOne;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveboardRoomService {

	private final LiveBoardRoomRepository roomRepository;
	private final LiveBoardConnectionRepository connectionRepository;
	private final MatchRepository matchRepository;

	// 🌟생성 - 이번주 매치데이터를 보고 라이브보드룸 전부 만들기
	public void createRoomsForWeek(LocalDate anyday) {
		// 1. 이번주 월요일 자정, 일요일 자정 지정찾아서 이번주 경기들 찾기
		LocalDateTime monday = anyday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
		LocalDateTime sunday = monday.plusDays(7).toLocalDate().atStartOfDay();
		List<Match> weekOfMatches = matchRepository.findAllByMatchTimeBetween(monday,sunday);

		// 2. 이번주 경기가 없다면 return
		if(weekOfMatches.isEmpty()) {
			return;
		}

		// 3. 매치ID들을 모아서 room 레파지토리에 기존재하는지 찾기
		Set<Long> matchIds
			= weekOfMatches.stream().map(Match::getId).collect(Collectors.toSet());
		Set<Long> alreadyCreated
			= roomRepository.findAllByMatchIdIn(matchIds).stream().map(LiveBoardRoom::getMatchId).collect(Collectors.toSet());

		for(Match match : weekOfMatches) {
			// 4-1. 매치ID에 대해 룸이 이미 생성되어 있다면 SKIP
			if(alreadyCreated.contains(match.getId())) continue;
			// 4-2.룸이 존재하지 않는 경우에 실행
			createRoom(match);
		}
	}

	// 🌟조회 - 이번주 라이브보드룸 전부 DTO로 조회하기
	private List<LiveBoardRoomResponseDto> getRoomsForWeek(LocalDate anyday) {

		LocalDateTime monday = anyday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
		LocalDateTime sunday = monday.plusDays(7).toLocalDate().atStartOfDay();
		List<Match> weekOfMatches = matchRepository.findAllByMatchTimeBetween(monday,sunday);

		Map<Long, LiveBoardRoom> roomMap = roomRepository.findAllRoom().stream()
			.filter(room -> {
				LocalDate matchDate = room.getMatchTime().toLocalDate();
				return !matchDate.isBefore(monday.toLocalDate()) && matchDate.isBefore(sunday.toLocalDate());
			})
			.collect(Collectors.toMap(LiveBoardRoom::getMatchId, Function.identity()));

		List<LiveBoardRoomResponseDto> roomDtosForWeek =
			weekOfMatches.stream().map(match -> {
				LiveBoardRoom room = roomMap.get(match.getId());
				long connectCount = 0L;
				if(room.getStatus()== LiveBoardStatus.TODAY) {
					connectCount = connectionRepository.getConnectionCount(room.getRoomId());
				}
				return LiveBoardRoomResponseDto.of(match, room, connectCount);
			}).toList();

		return roomDtosForWeek;
	}

	// 🌟수정 - 이번주 추가 경기 룸 생성, 상태 세팅
	public void refreshRoomsForWeek(LocalDate today) {
		LocalDateTime monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
		LocalDateTime sunday = monday.plusDays(7).toLocalDate().atStartOfDay();
		List<Match> weekOfMatches = matchRepository.findAllByMatchTimeBetween(monday,sunday);

		// 매치id - 룸이랑 매핑하기 <-- 필터로 하는것보다 효율적임
		Map<Long,LiveBoardRoom> roomMap = roomRepository.findAllRoom().stream().collect(Collectors.toMap(LiveBoardRoom::getMatchId, Function.identity()));

		for(Match match : weekOfMatches) {
			Long matchId = match.getId();
			LiveBoardRoom room = roomMap.get(matchId);

			// 특이상황 - 추가경기의 경우
			if(room == null) {
				room = createRoom(match);
			}

			LocalDate matchDate = match.getMatchTime().toLocalDate();
			// 당일 경기 - TODAY로
			if(matchDate.isEqual(today)) {
				room.updateStatusToToday();
			} else if (matchDate.isBefore(today)) { // 지난 경기 - PAST로
				room.updateStatusToPast();
			} else { // 나머지 - UPCOMING으로
				room.updateStatusToUpcoming();
			} roomRepository.saveRoom(room);
		}
	}

	// 🌟삭제 - 일주일치 룸 삭제
	public String deleteRoomsForWeek (LocalDate anyday) {
		LocalDateTime monday = anyday.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
		LocalDateTime sunday = monday.plusDays(7).toLocalDate().atStartOfDay();
		List<LiveBoardRoom> roomsToDelete = roomRepository.findAllRoom().stream().filter(room -> {
			LocalDateTime matchTime = room.getMatchTime();
			return !matchTime.isBefore(monday) && matchTime.isBefore(sunday);
		}).toList();

		for(LiveBoardRoom room : roomsToDelete) {
			roomRepository.deleteRoom(room.getRoomId());
			connectionRepository.deleteAllConnections(room.getRoomId());
		}

		String startDay = monday.format(DateTimeFormatter.ofPattern("MM/dd"));
		String endDay = sunday.format(DateTimeFormatter.ofPattern("MM/dd"));
		int count = roomsToDelete.size();

		return "[LIVEBOARD/ROOM]" + startDay + " ~ " + endDay + count + "개 룸 삭제 완료";
	}

	// 🌟삭제 - 날짜별 룸 삭제
	public String deleteRoomsByDate(LocalDate anyday) {
		LocalDateTime start = anyday.atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<LiveBoardRoom> roomsToDelete = roomRepository.findAllRoom().stream().filter(room -> {
			LocalDateTime matchTime = room.getMatchTime();
			return !matchTime.isBefore(start) && matchTime.isBefore(end);
		}).toList();

		for(LiveBoardRoom room : roomsToDelete) {
			roomRepository.deleteRoom(room.getRoomId());
			connectionRepository.deleteAllConnections(room.getRoomId());
		}

		String day = anyday.format(DateTimeFormatter.ofPattern("MM/dd"));
		int count = roomsToDelete.size();

		return "[LIVEBOARD/ROOM]" + day + "의 " + count + "개 룸 삭제 완료";
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
		return "DELETED"; // TODO: 날짜별로 지워지게
	}
}
