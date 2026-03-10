package com.sparta.spartatigers.domain.item.model;

import java.time.LocalDate;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.item.dto.request.CreateItemRequestDto;
import com.sparta.spartatigers.domain.item.dto.request.UpdateItemRequestDto;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "items")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "UNIQUE_USER_ITEM",
                    columnNames = {"user_id", "created_date"})
        },
        indexes = {
            @Index(
                    name = "idx_item_user_status_created",
                    columnList = "user_id, status, created_at DESC"),
            @Index(name = "idx_item_status_created_date", columnList = "status, created_date")
        })
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Column(columnDefinition = "TEXT")
    private String image;

    @Column private String seatInfo;

    @Column private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_date")
    private LocalDate createdDate;

    @Column private Double latitude;

    @Column private Double longitude;

    @Column private String address;

    @Version private Long version;

    public Item(
        ItemCategory category,
        String image,
        String seatInfo,
        String title,
        String description,
        Double latitude,
        Double longitude,
        String address,
        ItemStatus status,
        User user,
        LocalDate createdDate) {

        this.category = category;
        this.image = image;
        this.seatInfo = seatInfo;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = status;
        this.user = user;
        this.createdDate = createdDate;
    }

    public static Item of(CreateItemRequestDto dto, User user, String image) {
        return new Item(
            dto.category(),
            image,
            dto.seatInfo(),
            dto.title(),
            dto.description(),
            null,
            null,
            null,
            ItemStatus.REGISTERED,
            user,
            LocalDate.now());
    }

    public void validateUserIsOwner(User user) {

        if (!this.user.getId().equals(user.getId())) {
            throw new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND);
        }
    }

    public void validateSenderIsNotOwner(User sender) {

        if (this.user.getId().equals(sender.getId())) {
            throw new InvalidRequestException(ExceptionCode.CANNOT_REQUEST_OWN_ITEM);
        }
    }

    public void validateReceiverIsOwner(User receiver) {

        if (!this.user.getId().equals(receiver.getId())) {
            throw new InvalidRequestException(ExceptionCode.RECEIVER_NOT_OWNER);
        }
    }

    public void deleteItem() {
        this.status = ItemStatus.DELETED;
        this.createdDate = null;
    }

    public void updateItem(UpdateItemRequestDto request) {

        if (request.category() != null) {
            this.category = request.category();
        }

        if (request.title() != null) {
            this.title = request.title();
        }

        if (request.seatInfo() != null) {
            this.seatInfo = request.seatInfo();
        }

        if (request.description() != null) {
            this.description = request.description();
        }
    }

    public void complete() {
        this.status = ItemStatus.COMPLETED;
        this.createdDate = null;
    }

    public void reopen() {
        this.status = ItemStatus.REGISTERED;
    }

    public void fail() {
        this.status = ItemStatus.FAILED;
    }
}