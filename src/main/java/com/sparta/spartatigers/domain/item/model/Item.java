package com.sparta.spartatigers.domain.item.model;

import com.sparta.spartatigers.domain.common.entity.BaseEntity;
import com.sparta.spartatigers.domain.item.dto.request.CreateItemRequestDto;
import com.sparta.spartatigers.domain.user.model.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

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
                    columnList = "user_id, status, createdAt DESC"),
            @Index(name = "idx_item_status_created_date", columnList = "status, createdDate")
        })
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ItemCategory category;

    @Column private String image;

    @Column private String seatInfo;

    @Column private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    private ItemStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column private LocalDate createdDate;

    @Version private Long version;

    public Item(
        ItemCategory category,
        String image,
        String seatInfo,
        String title,
        String description,
        ItemStatus status,
        User user,
        LocalDate createdDate) {

        this.category = category;
        this.image = image;
        this.seatInfo = seatInfo;
        this.title = title;
        this.description = description;
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
            ItemStatus.REGISTERED,
            user,
            LocalDate.now());
    }
}