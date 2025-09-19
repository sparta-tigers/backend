package com.sparta.spartatigers.global.notification;

public interface NotificationSender {

    /**
     * 디스코드 운영 팀에 알림 메세지를 보냅니다.
     * @param subject 제목
     * @param content 내용
     */
    void send(String subject, String content);

}
