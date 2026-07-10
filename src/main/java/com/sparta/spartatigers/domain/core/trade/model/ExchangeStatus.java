package com.sparta.spartatigers.domain.core.trade.model;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ExchangeStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    COMPLETED;

    @JsonCreator
    public static ExchangeStatus parsing(String inputValue) {
        return Stream.of(ExchangeStatus.values())
                .filter(
                        exchangeStatus -> exchangeStatus.toString()
                                .equals(inputValue.toUpperCase()))
                .findFirst()
                .orElse(null);
    }
}