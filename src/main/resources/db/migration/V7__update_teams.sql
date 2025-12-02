-- V7__update_teams.sql
-- teams 스키마 통일 (아이디 기존과 동일)

INSERT INTO teams (team_id, created_at, updated_at, code, name, symbol_url) VALUES
                                                                          (1,  '2025-08-13 12:29:07.654387', '2025-08-13 12:29:07.654387', 'LG', 'LG',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/LG_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (2,  '2025-08-13 12:29:07.662855', '2025-08-13 12:29:07.662855', 'SK', 'SSG',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/SSG_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (3,  '2025-08-13 12:29:07.668755', '2025-08-13 12:29:07.668755', 'WO', '키움',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/%E1%84%8F%E1%85%B5%E1%84%8B%E1%85%AE%E1%86%B7_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (4,  '2025-08-13 12:29:07.673876', '2025-08-13 12:29:07.673876', 'KT', 'KT',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/KT_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (5,  '2025-08-13 12:29:07.679607', '2025-08-13 12:29:07.679607', 'HH', '한화',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/%E1%84%92%E1%85%A1%E1%86%AB%E1%84%92%E1%85%AA_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (6,  '2025-08-13 12:29:07.685302', '2025-08-13 12:29:07.685302', 'NC', 'NC',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/NC_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (7,  '2025-08-13 12:29:07.691355', '2025-08-13 12:29:07.691355', 'HT', 'KIA',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/KIA_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (8,  '2025-08-13 12:29:07.697961', '2025-08-13 12:29:07.697961', 'SS', '삼성',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/%E1%84%89%E1%85%A1%E1%86%B7%E1%84%89%E1%85%A5%E1%86%BC_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (9,  '2025-08-13 12:29:07.703927', '2025-08-13 12:29:07.703927', 'LT', '롯데',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/%E1%84%85%E1%85%A9%E1%86%BA%E1%84%83%E1%85%A6_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png'),

                                                                          (10, '2025-08-13 12:29:07.710590', '2025-08-13 12:29:07.710590', 'OB', '두산',
                                                                           'https://team-symbol-bucket.s3.ap-southeast-2.amazonaws.com/%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF/%E1%84%83%E1%85%AE%E1%84%89%E1%85%A1%E1%86%AB_%E1%84%89%E1%85%B5%E1%86%B7%E1%84%87%E1%85%A9%E1%86%AF.png');
