package com.sparta.spartatigers.domain.exchangerequest.model;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.stream.Stream;

public enum ExchangeStatus {
        PENDING,
        ACCEPTED,
        REJECTED,
        COMPLETED;

        @JsonCreator
        public static ExchangeStatus parsing(String inputValue) {
            return Stream.of(ExchangeStatus.values())
                    .filter(
                            exchangeStatus ->
                                    exchangeStatus.toString().equals(inputValue.toUpperCase()))
                    .findFirst()
                    .orElse(null);
        }
    }