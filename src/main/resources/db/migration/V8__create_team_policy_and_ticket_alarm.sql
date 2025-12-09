-- ===========================================
-- 1) TEAM_BOOKING_POLICY 테이블 생성
-- ===========================================

CREATE TABLE team_booking_policy (
                                     booking_policy_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     team_id BIGINT NOT NULL,

                                     membership VARCHAR(100) NOT NULL,                   -- 회원 등급 이름
                                     open_days_before INT NOT NULL,                      -- 며칠 전
                                     open_hour_of_day INT NOT NULL,                      -- 시
                                     open_min_of_day INT NOT NULL,                       -- 분

                                     apply_scope VARCHAR(30) NOT NULL,                   -- SINGLE_MATCH / SERIES_HOME
                                     series_count INT NOT NULL,                          -- 시리즈 경기 수 (1 또는 3)
                                     base_type VARCHAR(50) NOT NULL,                     -- MATCH_DATE / HOME_SERIES_FIRST_MATCH

                                     season_year INT NOT NULL,                           -- 시즌 (예: 2025)
                                     active TINYINT(1) NOT NULL DEFAULT 1,               -- 활성 여부

                                     ticket_url VARCHAR(500) NULL,                       -- 예매 URL

                                     CONSTRAINT fk_team_policy_team
                                         FOREIGN KEY (team_id) REFERENCES teams(team_id)
);

CREATE INDEX idx_team_policy_team ON team_booking_policy(team_id);
CREATE INDEX idx_team_policy_season ON team_booking_policy(season_year);


-- ===========================================
-- 2) TICKET_ALARMS 테이블 생성
-- ===========================================

CREATE TABLE ticket_alarms (
                               ticket_alarm_id BIGINT AUTO_INCREMENT PRIMARY KEY,

                               user_id BIGINT NOT NULL,
                               match_id BIGINT NOT NULL,
                               booking_policy_id BIGINT NOT NULL,

                               minus_before INT NOT NULL,                          -- 알람 몇 분 전
                               alarm_time DATETIME(6) NOT NULL,                    -- 실제 알람 시간
                               open_booking_time DATETIME(6) NOT NULL,

                               created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                               updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

                               CONSTRAINT fk_ticket_alarm_user
                                   FOREIGN KEY (user_id) REFERENCES users(user_id),

                               CONSTRAINT fk_ticket_alarm_match
                                   FOREIGN KEY (match_id) REFERENCES matches(match_id),

                               CONSTRAINT fk_ticket_alarm_policy
                                   FOREIGN KEY (booking_policy_id) REFERENCES team_booking_policy(booking_policy_id)
);

CREATE INDEX idx_ticket_alarm_user ON ticket_alarms(user_id);
CREATE INDEX idx_ticket_alarm_match ON ticket_alarms(match_id);
CREATE INDEX idx_ticket_alarm_policy ON ticket_alarms(booking_policy_id);
