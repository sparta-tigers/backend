package com.sparta.spartatigers.domain.item.repository;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.InvalidRequestException;

import jakarta.persistence.LockModeType;
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

public interface ItemRepository extends JpaRepository<Item, Long> {

    @EntityGraph(attributePaths = "user")
    @Query("select i from items i where i.status = :itemStatus and i.createdDate = :createdDate and i.user.id in :nearByUserIds")
    Page<Item> findAllItems(@Param("itemStatus") ItemStatus itemStatus, @Param("createdDate") LocalDate createdDate, @Param("nearByUserIds") List<Long> nearByUserIds, Pageable pageable);

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
