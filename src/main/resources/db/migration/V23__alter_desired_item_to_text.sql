-- PR 리뷰 반영: desiredItem(희망 아이템) 컬럼 타입 확장
-- 사용자가 문장형으로 상세히 입력할 수 있도록 VARCHAR(255) → TEXT로 변경
--
-- [사전 점검 가이드 - V22 적용 전 운영 환경에서 반드시 확인]
-- 1. 중복 REGISTERED 아이템 점검 쿼리 (V22 마이그레이션 실패 방지):
--    SELECT user_id, COUNT(*)
--    FROM items
--    WHERE status = 'REGISTERED'
--    GROUP BY user_id
--    HAVING COUNT(*) > 1;
--    → 결과가 있으면 중복 데이터를 정리(UPDATE items SET status='DELETED' WHERE id IN (...))한 후 마이그레이션을 실행하세요.
--
-- 2. UNIQUE_USER_ITEM 인덱스 존재 여부 확인 (V22 DROP INDEX 실패 방지):
--    SHOW INDEX FROM items WHERE Key_name = 'UNIQUE_USER_ITEM';
--    → 인덱스가 없는 환경에서 V22를 최초 적용하는 경우 오류가 발생할 수 있습니다.
--    → 해당 환경에서는 V22의 DROP INDEX 구문을 수동으로 스킵하거나 IF EXISTS 처리 후 진행하세요.

ALTER TABLE items MODIFY COLUMN desired_item TEXT NULL;
