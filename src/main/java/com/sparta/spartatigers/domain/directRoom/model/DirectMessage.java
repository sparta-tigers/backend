package com.sparta.spartatigers.domain.directRoom.model;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.foundation.user.account.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "direct_message")
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DirectMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "direct_message_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "direct_room_id", nullable = false)
    private DirectRoom directRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private LocalDateTime sentAt;

    @Column (nullable = false)
    private boolean isRead;

    public static DirectMessage of(DirectRoom directRoom, User sender, String message, LocalDateTime sentAt) {
        DirectMessage directMessage = new DirectMessage();
        directMessage.directRoom = directRoom;
        directMessage.sender = sender;
        directMessage.message = message;
        directMessage.sentAt = sentAt;
        directMessage.isRead = false;
        return directMessage;
    }

    // 읽음 처리용 메서드
    public void markAsRead() {
        this.isRead = true;
    }

}