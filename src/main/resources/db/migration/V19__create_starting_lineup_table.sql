SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE starting_line_up (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  match_id BIGINT NOT NULL,
                                  team_code VARCHAR(50),
                                  batting_order INT,
                                  position VARCHAR(50),
                                  player_name VARCHAR(100),
    -- REFERENCES matches(id)를 (match_id)로 수정!
                                  CONSTRAINT fk_starting_lineup_match FOREIGN KEY (match_id) REFERENCES matches(match_id)
);

SET FOREIGN_KEY_CHECKS = 1;