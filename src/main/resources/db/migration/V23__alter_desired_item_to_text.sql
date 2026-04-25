-- PR 리뷰 반영: desiredItem(희망 아이템) 컬럼 타입 확장
-- 사용자가 문장형으로 상세히 입력할 수 있도록 VARCHAR(255) → TEXT로 변경

ALTER TABLE items MODIFY COLUMN desired_item TEXT NULL;
