-- 기존 테이블 제거
DROP TABLE IF EXISTS favorite_team;

-- 새 테이블 생성
CREATE TABLE favorite_team (
                               user_id BIGINT NOT NULL,                -- PK로 직접 사용
                               created_at DATETIME(6) NOT NULL,
                               updated_at DATETIME(6) DEFAULT NULL,
                               team_id BIGINT NOT NULL,
                               PRIMARY KEY (user_id),                  -- PK는 user_id
                               UNIQUE KEY UK_favteam_user (user_id),   -- 유저당 하나만 등록
                               KEY FK_favteam_team (team_id),
                               CONSTRAINT FK_favteam_user FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
                               CONSTRAINT FK_favteam_team FOREIGN KEY (team_id) REFERENCES teams (team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
