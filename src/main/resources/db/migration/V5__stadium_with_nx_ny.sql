-- 1️⃣ 기존 stadium 테이블 삭제
DROP TABLE IF EXISTS stadium;

-- 2️⃣ 새 stadium 테이블 생성 (nx, ny 포함)
CREATE TABLE stadium (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         latitude DOUBLE NOT NULL,
                         longitude DOUBLE NOT NULL,
                         nx INT NOT NULL COMMENT '기상청 격자 X좌표',
                         ny INT NOT NULL COMMENT '기상청 격자 Y좌표',
                         name VARCHAR(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3️⃣ 기본 구장 데이터 삽입 (기상청 격자 포함)
INSERT INTO stadium (latitude, longitude, nx, ny, name) VALUES
                                                            (35.193900, 129.061600, 98, 76, '사직'),
                                                            (37.512150, 127.071976, 62, 126, '잠실'),
                                                            (37.435100, 126.690700, 55, 124, '문학'),
                                                            (35.841000, 128.681600, 91, 90, '대구'),
                                                            (37.299700, 127.009800, 61, 121, '수원'),
                                                            (35.168100, 126.889100, 59, 74, '광주'),
                                                            (37.498200, 126.867100, 58, 125, '고척'),
                                                            (36.317000, 127.429200, 68, 100, '대전(신)'),
                                                            (35.222500, 128.582300, 89, 77, '창원'),
                                                            (36.007780, 129.359440, 102, 94, '포항'),
                                                            (35.532032, 129.265762, 101, 84, '울산');
