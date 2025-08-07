package com.sparta.spartatigers.domain.item.repository;

import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.item.model.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from items i where i.status = :itemStatus")
    Page<Item> findAllItems(@Param("itemStatus") ItemStatus itemStatus, Pageable pageable);
}
