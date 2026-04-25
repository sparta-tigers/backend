-- 1. 기존 유니크 제약 조건 삭제
ALTER TABLE items DROP INDEX UNIQUE_USER_ITEM;

-- 2. 'REGISTERED' 상태인 경우에만 적용되는 조건부 유니크 인덱스 생성
-- MySQL 8.0.13+ Functional Index 활성화
-- 상태가 'REGISTERED' 아닐 경우(DELETED 등) NULL을 반환하여 Unique 제약에서 제외됩니다.
ALTER TABLE items ADD UNIQUE INDEX UK_ACTIVE_ITEM_PER_USER (user_id, (IF(status = 'REGISTERED', status, NULL)));
