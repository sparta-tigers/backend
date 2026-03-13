-- 1. \uae30\uc874 \uc720\ub2c8\ud06c \uc81c\uc57d \uc870\uac74 \uc0ad\uc81c
ALTER TABLE items DROP INDEX UNIQUE_USER_ITEM;

-- 2. 'REGISTERED' \uc0c1\ud0dc\uc778 \uacbd\uc6b0\uc5d0\ub9cc \uc801\uc6a9\ub418\ub224 \uc870\uac74\ubd80 \uc720\ub2c8\ud06c \uc778\ub371\uc2a4 \uc0dd\uc131
-- MySQL 8.0+ Functional Index \ud65c\uc6a9
-- \uc0c1\ud0dc\uac00 'REGISTERED' \uc544\ub2cc \uacbd\uc6b0(DELETED \ub4f1)\ub224 NULL\uc744 \ubc18\ud658\ud558\uc5ec Unique \uc81c\uc57d\uc5d0\uc11c \uc81c\uc678\ub419\ub2c8\ub2e4.
ALTER TABLE items ADD UNIQUE INDEX UK_ACTIVE_ITEM_PER_USER (user_id, (IF(status = 'REGISTERED', status, NULL)));
