-- 1. 외래 키 체크 일시 해제 (안전한 테이블 삭제를 위함)
SET FOREIGN_KEY_CHECKS = 0;

-- 2. 기존의 잘못된 테이블 삭제
DROP TABLE IF EXISTS starting_line_up;
DROP TABLE IF EXISTS starting_lineup;
DROP TABLE IF EXISTS lineup_player;

-- 3. StartingLineup 테이블 생성 (부모)
-- @EmbeddedId와 @MapsId 구조를 반영하여 match_id와 team_id를 복합 PK로 설정
CREATE TABLE starting_lineup (
                                 match_id BIGINT NOT NULL,
                                 team_id BIGINT NOT NULL,
                                 starting_pitcher VARCHAR(255),
                                 PRIMARY KEY (match_id, team_id),
                                 CONSTRAINT fk_starting_lineup_match FOREIGN KEY (match_id) REFERENCES matches (match_id),
                                 CONSTRAINT fk_starting_lineup_team FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE=InnoDB;

-- 4. LineupPlayer 테이블 생성 (자식)
-- 부모의 복합키(match_id, team_id)를 외래 키로 참조하는 구조
CREATE TABLE lineup_player (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               match_id BIGINT NOT NULL,
                               team_id BIGINT NOT NULL,
                               batting_order INT NOT NULL,
                               player_name VARCHAR(100) NOT NULL,
                               position VARCHAR(50) NOT NULL, -- Enum(Position) 저장용

    -- 부모 테이블의 복합 PK 참조 (@JoinColumns 매핑)
                               CONSTRAINT fk_lineup_player_parent FOREIGN KEY (match_id, team_id)
                                   REFERENCES starting_lineup (match_id, team_id),

    -- 엔티티의 @UniqueConstraint 반영: 한 경기 내 특정 팀의 타순 중복 방지
                               CONSTRAINT match_team_batting_order UNIQUE (match_id, team_id, batting_order)
) ENGINE=InnoDB;

-- 5. 외래 키 체크 다시 설정
SET FOREIGN_KEY_CHECKS = 1;