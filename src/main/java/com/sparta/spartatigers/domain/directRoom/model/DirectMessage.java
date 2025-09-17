package com.sparta.spartatigers.domain.directRoom.model;

import java.time.LocalDateTime;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
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

    @PrePersist
    protected void onPersist() {
        this.sentAt = LocalDateTime.now();
    }

    public DirectMessage(DirectRoom directRoom, User sender, String message, LocalDateTime sentAt) {
        this.directRoom = directRoom;
        this.sender = sender;
        this.message = message;
        this.sentAt = sentAt;
    }

}