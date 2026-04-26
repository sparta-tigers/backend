-- 기획서 상의 희망 아이템(WANT) 필드 추가
ALTER TABLE items ADD COLUMN desired_item VARCHAR(255) NULL;
