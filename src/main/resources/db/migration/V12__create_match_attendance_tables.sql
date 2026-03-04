-- 혹시 모를 충돌을 방지하기 위해 외래키 무결성 검사 임시 해제
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 직관 기록 (match_attendance) 테이블 생성
CREATE TABLE match_attendance (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  user_id BIGINT,
                                  match_id BIGINT,
                                  contents TEXT,
                                  seat VARCHAR(255),

    -- BaseEntity 필드
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL
);

-- 2. 직관 기록 이미지 (attendance_image) 테이블 생성
CREATE TABLE attendance_image (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  attendance_id BIGINT NOT NULL,
                                  s3url VARCHAR(2048),
                                  image_type VARCHAR(50),

    -- BaseEntity 필드
                                  created_at DATETIME(6) NOT NULL,
                                  updated_at DATETIME(6) NOT NULL,

    -- 외래키 제약조건 (attendance 삭제 시 연관된 이미지도 DB에서 삭제)
                                  CONSTRAINT fk_attendance_image_attendance FOREIGN KEY (attendance_id) REFERENCES match_attendance (id) ON DELETE CASCADE
);

-- (선택) 조회 성능을 위한 인덱스 추가
CREATE INDEX idx_match_attendance_user_id ON match_attendance (user_id);
CREATE INDEX idx_match_attendance_match_id ON match_attendance (match_id);

-- items 테이블의 status ENUM에 'DELETED' 값 추가
ALTER TABLE `items`
    MODIFY COLUMN `status` ENUM('COMPLETED', 'FAILED', 'REGISTERED', 'DELETED') COLLATE utf8mb4_unicode_ci DEFAULT NULL;

-- 테이블 생성이 끝난 후 외래키 무결성 검사 다시 활성화
SET FOREIGN_KEY_CHECKS = 1;