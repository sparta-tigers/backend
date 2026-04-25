package com.sparta.spartatigers.domain.item.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.InvalidRequestException;

import jakarta.persistence.LockModeType;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select i from items i where i.status = :itemStatus and i.createdDate = :createdDate and i.user.id in :nearByUserIds")
    Page<Item> findAllItems(@Param("itemStatus") ItemStatus itemStatus, @Param("createdDate") LocalDate createdDate, @Param("nearByUserIds") List<Long> nearByUserIds, Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query(value = "SELECT i FROM items i WHERE i.status = :itemStatus AND i.createdDate = :createdDate AND " +
           // [FIX] latitude/longitude가 null인 아이템은 Haversine 식에서 자동 제외되지만,
           // NULL 좌표 처리 의도를 명시적으로 드러내어 유지보수성을 향상시킵니다.
           "i.latitude IS NOT NULL AND i.longitude IS NOT NULL AND " +
           // [FIX] 본인 아이템 제외 — findAllItems 경로(nearByUserIds.add(userId))와 동작 일치
           "i.user.id != :userId AND " +
           "(6371 * acos(cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
           "cos(radians(i.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(i.latitude))) <= :radius)",
           countQuery = "SELECT count(i) FROM items i WHERE i.status = :itemStatus AND i.createdDate = :createdDate AND " +
                       "i.latitude IS NOT NULL AND i.longitude IS NOT NULL AND " +
                       "i.user.id != :userId AND " +
                       "(6371 * acos(cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
                       "cos(radians(i.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                       "sin(radians(i.latitude))) <= :radius)")
    Page<Item> findAllItemsByLocation(
            @Param("itemStatus") ItemStatus itemStatus,
            @Param("createdDate") LocalDate createdDate,
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Double radius,
            Pageable pageable);

    @EntityGraph(attributePaths = "user")
    @Query("select i from items i where i.status = :itemStatus and i.createdDate = :createdDate and i.user.id = :userId")
    Page<Item> findAllMyItems(
        @Param("itemStatus") ItemStatus itemStatus,
        @Param("createdDate") LocalDate createdDate,
        @Param("userId") Long userId,
        Pageable pageable);
    
    boolean existsByUserIdAndStatus(Long userId, ItemStatus status);

    Optional<Item> findByIdAndStatusAndCreatedDate(Long id, ItemStatus itemStatus, LocalDate createdDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from items i where i.id = :id and i.status = :itemStatus")
    Optional<Item> findByIdWithLock(@Param("id") Long id, @Param("itemStatus") ItemStatus itemStatus);

    default Item findByIdWithLockOrElseThrow(Long id) {

        return findByIdWithLock(id, ItemStatus.REGISTERED)
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND));
    }

    default Item findByIdAndStatusAndDateOrElseThrow(Long id) {

        return findByIdAndStatusAndCreatedDate(id, ItemStatus.REGISTERED,
            LocalDate.now()).orElseThrow(
            () -> new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND));
    }
}
