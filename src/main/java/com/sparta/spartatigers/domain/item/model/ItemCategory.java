package com.sparta.spartatigers.domain.item.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

public enum ItemCategory {
        GOODS,
        TICKET;

        @JsonCreator
        public static ItemCategory parsing(String inputValue) {
            return Stream.of(ItemCategory.values())
                    .filter(category -> category.toString().equals(inputValue.toUpperCase()))
                    .findFirst()
                    .orElse(null);
        }
    }