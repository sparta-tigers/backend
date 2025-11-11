-- PK 이름 변경
ALTER TABLE exchange_request
    CHANGE COLUMN match_id exchange_request_id BIGINT NOT NULL AUTO_INCREMENT;

-- FK 삭제 & 재생성
ALTER TABLE direct_rooms
    DROP FOREIGN KEY FK9yatw1d8gdw0rccfvp5691u1l;

ALTER TABLE direct_rooms
    ADD CONSTRAINT FK9yatw1d8gdw0rccfvp5691u1l
        FOREIGN KEY (exchange_request_id)
            REFERENCES exchange_request (exchange_request_id);
