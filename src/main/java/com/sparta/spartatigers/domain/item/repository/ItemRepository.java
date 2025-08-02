package com.sparta.spartatigers.domain.item.repository;

import com.sparta.spartatigers.domain.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

}
