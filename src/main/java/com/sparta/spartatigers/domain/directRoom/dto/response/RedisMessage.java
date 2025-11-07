package com.sparta.spartatigers.domain.directRoom.dto.response;

import com.sparta.spartatigers.domain.directRoom.model.DirectMessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RedisMessage {

    private Long senderId;
    private Long roomId;
    private Long messageId;
    private String message;
    private String
            sentAt; // redis 발행할 때 json으로 변환 실패할 수도 있음(RedisDirectMessageSubscriber 역직렬화 할 때 실패할 수도
    // 있어서 String 타입)
    private String senderNickname;

    public static RedisMessage from(DirectMessage message) {
        return new RedisMessage(
                message.getSender().getId(),
                message.getDirectRoom().getId(),
                message.getId(),
                message.getMessage(),
                message.getSentAt().toString(),
                message.getSender().getNickname());
    }
}
