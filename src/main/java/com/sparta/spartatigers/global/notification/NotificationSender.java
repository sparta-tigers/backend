package com.sparta.spartatigers.global.notification;

import com.sparta.spartatigers.global.notification.dto.MessagePayload;

public interface NotificationSender {

    /**
     * 디스코드 운영 팀에 알림 메세지를 보냅니다.
     *
     * @param payload 메세지에 대한 정보
     */
    void send(MessagePayload payload);

}
