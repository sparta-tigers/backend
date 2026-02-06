-- 1. 컬럼 추가
ALTER TABLE matches
    ADD COLUMN season_year INT NULL,
    ADD COLUMN league_type ENUM('REGULAR', 'POST_SEASON', 'PRESEASON') NULL;

-- 2. 기존 데이터 보정
UPDATE matches
SET
    season_year = YEAR(match_time),
    league_type = 'REGULAR'
WHERE match_time IS NOT NULL
  AND season_year IS NULL;

-- (혹시라도 match_time NULL 대비용)
UPDATE matches
SET
    league_type = 'REGULAR',
    season_year = season_year /* or 정책값 */
WHERE league_type IS NULL;

-- 3. 제약
ALTER TABLE matches
    MODIFY season_year INT NOT NULL,
    MODIFY league_type ENUM('REGULAR', 'POST_SEASON', 'PRESEASON') NOT NULL;
