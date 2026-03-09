package com.sparta.spartatigers.domain.exchangerequest.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.ServerException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity(name = "exchange_request")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "exchange_request_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private ExchangeStatus status;

    private String have;

    public ExchangeRequest(Item item, User sender, User receiver, ExchangeStatus exchangeStatus, String have) {
        this.item = item;
        this.sender = sender;
        this.receiver = receiver;
        this.status = exchangeStatus;
        this.have = have;
    }

    public static ExchangeRequest of(Item item, User sender, User receiver, String have) {

        return new ExchangeRequest(item, sender, receiver, ExchangeStatus.PENDING, have);
    }

    public void validateReceiverIsOwner(User receiver) {

        if (!this.receiver.getId().equals(receiver.getId())) {
            throw new ServerException(ExceptionCode.RECEIVER_FORBIDDEN);
        }
    }

    public void updateStatus(ExchangeStatus status) {
        this.status = status;
    }

    public void complete() {
        this.status = ExchangeStatus.COMPLETED;
    }
}