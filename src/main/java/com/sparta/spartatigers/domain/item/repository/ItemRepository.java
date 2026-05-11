package com.sparta.spartatigers.domain.item.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
           // [FIX] 공간 쿼리 최적화: 삼각함수 연산 전 인덱스를 탈 수 있는 Bounding Box Prefilter 적용
           "i.latitude BETWEEN :minLat AND :maxLat AND i.longitude BETWEEN :minLon AND :maxLon AND " +
           "i.user.id != :userId AND " +
           // [FIX] 부동소수점 오차 방어: acos() 인자가 1.0을 초과하지 않도록 LEAST(1.0, ...) 처리
           "(6371 * acos(LEAST(1.0, cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
           "cos(radians(i.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
           "sin(radians(i.latitude)))) <= :radius)",
           countQuery = "SELECT count(i) FROM items i WHERE i.status = :itemStatus AND i.createdDate = :createdDate AND " +
                        "i.latitude BETWEEN :minLat AND :maxLat AND i.longitude BETWEEN :minLon AND :maxLon AND " +
                        "i.user.id != :userId AND " +
                        "(6371 * acos(LEAST(1.0, cos(radians(:latitude)) * cos(radians(i.latitude)) * " +
                        "cos(radians(i.longitude) - radians(:longitude)) + sin(radians(:latitude)) * " +
                        "sin(radians(i.latitude)))) <= :radius)")
    Page<Item> findAllItemsByLocation(
            @Param("itemStatus") ItemStatus itemStatus,
            @Param("createdDate") LocalDate createdDate,
            @Param("userId") Long userId,
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("minLat") Double minLat,
            @Param("maxLat") Double maxLat,
            @Param("minLon") Double minLon,
            @Param("maxLon") Double maxLon,
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

    default Item findByIdAndStatusAndDateOrElseThrow(Long id, LocalDate createdDate) {
        return findByIdAndStatusAndCreatedDate(id, ItemStatus.REGISTERED, createdDate)
            .orElseThrow(() -> new InvalidRequestException(ExceptionCode.ITEM_NOT_FOUND));
    }

    // 고아 이미지 정리용: DB에 참조된 모든 이미지 URL 조회
    @Query("SELECT i.image FROM items i WHERE i.image IS NOT NULL")
    Stream<String> findAllImageUrls();
}
