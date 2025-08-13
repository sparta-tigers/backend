package com.sparta.spartatigers.domain.item.repository;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import com.sparta.spartatigers.global.error.CustomException;
import com.sparta.spartatigers.global.error.ErrorType;
import jakarta.persistence.LockModeType;
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
    @Query("select i from items i where i.status = :itemStatus")
    Page<Item> findAllItems(@Param("itemStatus") ItemStatus itemStatus, Pageable pageable);

    Optional<Item> findByIdAndStatus(Long id, ItemStatus itemStatus);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from items i where i.id = :id and i.status = :itemStatus")
    Optional<Item> findByIdWithLock(@Param("id") Long id, @Param("itemStatus") ItemStatus itemStatus);

    default Item findByIdWithLockOrElseThrow(Long id) {

        return findByIdWithLock(id, ItemStatus.REGISTERED)
            .orElseThrow(() -> new CustomException(ErrorType.ITEM_NOT_FOUND));
    }
}
