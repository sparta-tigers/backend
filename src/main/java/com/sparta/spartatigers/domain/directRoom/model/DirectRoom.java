package com.sparta.spartatigers.domain.directRoom.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "direct_rooms")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DirectRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_room_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exchange_request_id", nullable = false)
    private ExchangeRequest exchangeRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // TODO: 나중에 확장 기능에서 교환 완료 시점에 채팅방을 readOnly로 바꿀 수 있게 보류
    @Column(nullable = false)
    private boolean isCompleted = false;

    private LocalDateTime completedAt;

    public static DirectRoom create(ExchangeRequest exchangeRequest, User sender, User receiver) {
        DirectRoom room = new DirectRoom();
        room.exchangeRequest = exchangeRequest;
        room.sender = sender;
        room.receiver = receiver;
        return room;
    }

    public void complete() {
        this.isCompleted = true;
        this.completedAt = LocalDateTime.now();
    }
}