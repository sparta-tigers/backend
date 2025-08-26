package com.sparta.spartatigers.domain.item.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import com.sparta.spartatigers.global.exception.ExceptionCode;
import com.sparta.spartatigers.global.exception.InvalidRequestException;

import java.util.stream.Stream;

public enum ItemCategory {
        GOODS,
        TICKET;

        @JsonCreator
        public static ItemCategory parsing(String inputValue) {
            return Stream.of(ItemCategory.values())
                    .filter(category -> category.toString().equals(inputValue.toUpperCase()))
                    .findFirst()
                    .orElseThrow(() -> new InvalidRequestException(ExceptionCode.VALIDATION_ERROR));
        }
    }