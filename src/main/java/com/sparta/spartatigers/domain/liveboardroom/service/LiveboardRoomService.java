package com.sparta.spartatigers.domain.liveboardroom.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.sparta.spartatigers.domain.liveboardroom.dto.LiveBoardRoomResponseDto;
import com.sparta.spartatigers.domain.liveboardroom.model.LiveBoardRoom;
import com.sparta.spartatigers.domain.liveboardroom.repository.LiveBoardConnectionRepository;
import com.sparta.spartatigers.domain.liveboardroom.repository.LiveBoardRoomRepository;
import com.sparta.spartatigers.domain.match.model.Match;
import com.sparta.spartatigers.domain.match.repository.MatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LiveboardRoomService {

	private final LiveBoardRoomRepository roomRepository;
	private final LiveBoardConnectionRepository connectionRepository;
	private final MatchRepository matchRepository;

	// 라이브 보드룸 생성
	public String createTodayRoom() {

		// 오늘 경기 일정 찾기
		LocalDateTime start = LocalDate.now().atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<Match> matches = matchRepository.findAllByMatchTimeBetween(start, end);

		boolean alreadyCreated = false;

		// 라이브 보드룸 생성 후 저장
		for (Match match : matches) {
			String roomId = "ROOM_" + match.getId();

			if (roomRepository.existsById(roomId)) { // 중복 여부 확인 (생성 막지는 않음)
				alreadyCreated = true;
			}

			String title = match.getAwayTeam().getName() + "VS" + match.getHomeTeam().getName();
			LocalDateTime matchTime = match.getMatchTime();

			LiveBoardRoom room = LiveBoardRoom.of(roomId, match.getId(), title, matchTime);

			roomRepository.saveRoom(room);
		}

		return alreadyCreated ? "ALREADY_CREATED" : "CREATED";
	}

	// 라이브 보드룸 전체 조회
	public List<LiveBoardRoomResponseDto> findAllRooms() {
		return roomRepository.findAllRoom().stream()
			.map(
				room -> {
					Long count = connectionRepository.getConnectionCount(room.getRoomId());
					return LiveBoardRoomResponseDto.of(room, count);
				})
			.collect((Collectors.toList()));
	}

	// 오늘의 라이브 보드룸 조회
	public List<LiveBoardRoomResponseDto> findTodayRooms() {
		LocalDateTime start = LocalDate.now().atStartOfDay();
		LocalDateTime end = start.plusDays(1);
		List<Match> matches = matchRepository.findAllByMatchTimeBetween(start, end);
		List<LiveBoardRoomResponseDto> rooms =
			roomRepository.findAllRoom().stream()
				.filter(
					room ->
						!room.getOpenAt().isBefore(start)
							&& room.getOpenAt().isBefore(end))
				.map(
					room -> {
						Long count =
							connectionRepository.getConnectionCount(
								room.getRoomId());
						return LiveBoardRoomResponseDto.of(room, count);
					})
				.toList();

		return rooms;
	}

	// 경기 방이 안생기면 데이터를 제대로 못가지고 옴
	public List<LiveBoardRoomResponseDto> findRoomsByDate(LocalDate start) {
		LocalDateTime end = start.plusDays(1).atStartOfDay();
		List<Match> matches = matchRepository.findAllByMatchTimeBetween(start.atStartOfDay(), end);
		List<LiveBoardRoomResponseDto> rooms =
			roomRepository.findAllRoom().stream()
				.filter(
					room ->
						!room.getOpenAt().isBefore(start.atStartOfDay())
							&& room.getOpenAt().isBefore(end))
				.map(
					room -> {
						Long count =
							connectionRepository.getConnectionCount(
								room.getRoomId());
						return LiveBoardRoomResponseDto.of(room, count);
					})
				.toList();

		List<LiveBoardRoomResponseDto> liveBoardRoomResponseDtos = new ArrayList<>();
		// 리스트로 제목이 같으면 합친다
		// match.getAwayTeam().getName() + "VS" + match.getHomeTeam().getName()
		// TODO 이거도 매치별로 룸을 넣어주기, 룸이 없을 수도 있겠다.
		for (int i = 0; i < matches.size(); i++) {
			LiveBoardRoomResponseDto dto = null;

			boolean findRooms = false;
			for (int j = 0; j < rooms.size(); j++) {
				if (rooms.get(j)
					.getTitle()
					.equals(
						matches.get(i).getAwayTeam().getName()
							+ "VS"
							+ matches.get(i).getHomeTeam().getName())) {
					findRooms = true;

					dto =
						LiveBoardRoomResponseDto.builder()
							.matchId(matches.get(i).getId())
							.title(
								matches.get(i).getAwayTeam().getName()
									+ "VS"
									+ matches.get(i).getHomeTeam().getName())
							.roomId(rooms.get(j).getRoomId())
							.connectCount(rooms.get(j).getConnectCount())
							.startedAt(matches.get(i).getMatchTime())
							.awayTeamCode(matches.get(i).getAwayTeam().getCode())
							.homeTeamCode(matches.get(i).getHomeTeam().getCode())
							.awayTeamName(matches.get(i).getAwayTeam().getName())
							.homeTeamName(matches.get(i).getHomeTeam().getName())
							.matchResult(matches.get(i).getMatchResult())
							.position(matches.get(i).getStadium().getName())
							.build();

					break;
				}
			}

			if (findRooms == false) {
				dto =
					LiveBoardRoomResponseDto.builder()
						.matchId(matches.get(i).getId())
						.title(
							matches.get(i).getAwayTeam().getName()
								+ "VS"
								+ matches.get(i).getHomeTeam().getName())
						.connectCount(0L)
						.startedAt(matches.get(i).getMatchTime())
						.awayTeamCode(matches.get(i).getAwayTeam().getCode())
						.homeTeamCode(matches.get(i).getHomeTeam().getCode())
						.awayTeamName(matches.get(i).getAwayTeam().getName())
						.homeTeamName(matches.get(i).getHomeTeam().getName())
						.matchResult(matches.get(i).getMatchResult())
						.position(matches.get(i).getStadium().getName())
						.build();
			}

			liveBoardRoomResponseDtos.add(dto);
		}

		return liveBoardRoomResponseDtos;
	}

	public String deleteRoom(String roomId) {
		LiveBoardRoom room = roomRepository.findRoomById(roomId);

		if (room == null) {
			return "ALREADY_DELETED";
		}
		roomRepository.deleteRoom(roomId);
		return "DELETED";
	}
}
