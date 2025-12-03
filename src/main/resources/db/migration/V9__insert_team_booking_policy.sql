-- V9__insert_team_booking_policy.sql
-- 구단별 예매정책 추가

INSERT INTO team_booking_policy
(booking_policy_id, team_id, membership, open_days_before, open_hour_of_day, open_min_of_day, apply_scope, series_count, base_type, season_year, active, ticket_url)
VALUES
-- LG (team_id = 1)
(1, 1, '일반', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(2, 1, '연간회원', 8, 14, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(3, 1, '장기회원', 9, 14, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),

-- SSG (team_id = 2)
(4, 2, '일반', 4, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(5, 2, 'Rookie', 5, 12, 1, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(6, 2, 'Regular', 6, 13, 2, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(7, 2, 'All Star', 7, 14, 3, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(8, 2, 'Legend', 7, 15, 4, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(9, 2, 'Frontier', 7, 16, 5, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),

-- 키움 (team_id = 3)
(10, 3, '일반', 7, 14, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(11, 3, '멤버십', 7, 12, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(12, 3, '연간 회원', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),

-- KT (team_id = 4)
(13, 4, '일반', 7, 14, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://tickets.interpark.com/'),
(14, 4, 'wizzap 일반 회원', 7, 12, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(15, 4, '어린이 회원', 7, 12, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(16, 4, '빅또리 회원', 8, 15, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(17, 4, '매직회원', 8, 14, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(18, 4, '시즌권 회원', 8, 13, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),

-- 한화 (team_id = 5)
(19, 5, '일반', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(20, 5, 'Early', 8, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),

-- NC (team_id = 6)
(21, 6, '일반', 6, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(22, 6, '민트 (월 구독)', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),
(23, 6, '민트 (연간)', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://official.com/'),

-- KIA (team_id = 7)
(24, 7, '일반', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://www.ticketlink.co.kr/'),
(25, 7, '블랙 등급 회원', 7, 10, 30, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://app.official.com/'),
(26, 7, '시즌권 회원', 7, 10, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://app.official.com/'),

-- 삼성 (team_id = 8)
(27, 8, '일반', 7, 11, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://www.ticketlink.co.kr/'),
(28, 8, '선예매권', 8, 11, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://app.official.com/'),
(29, 8, '블루 시즌권', 9, 14, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://app.official.com/'),
(30, 8, '프리미엄 블루 시즌권', 9, 11, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://app.official.com/'),

-- 롯데 (team_id = 9)
(31, 9, '일반', 7, 14, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://official.com/'),
(32, 9, '선예매 멤버십', 7, 10, 0, 'HOME_SERIES', 3, 'HOME_SERIES_FIRST_MATCH', 2025, true, 'https://official.com/'),

-- 두산 (team_id = 10)
(33, 10, '일반', 7, 11, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://tickets.interpark.com/'),
(34, 10, '베어스 클럽', 7, 10, 0, 'SINGLE_MATCH', 1, 'MATCH_DATE', 2025, true, 'https://tickets.interpark.com/');
