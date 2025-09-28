package com.sparta.spartatigers.domain.exchangerequest.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeStatus;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.ServerException;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    boolean existsBySenderIdAndReceiverIdAndItemId(Long senderId, Long receiverId, Long itemId);

    @EntityGraph(attributePaths = {"receiver", "sender", "item"})
    @Query("select e from exchange_request e where e.receiver.id = :receiverId")
    Page<ExchangeRequest> findAllReceiveRequest(@Param("receiverId") Long receiverId, Pageable pageable);

    Optional<ExchangeRequest> findByIdAndStatus(
        Long exchangeRequestId, ExchangeStatus status);

    default ExchangeRequest findExchangeRequestByIdOrElseThrow(Long exchangeRequestId) {
        return findByIdAndStatus(exchangeRequestId, ExchangeStatus.PENDING)
            .orElseThrow(() -> new ServerException(ExceptionCode.EXCHANGE_REQUEST_NOT_FOUND));
    }

    default ExchangeRequest findAcceptedRequestByIdOrElseThrow(Long exchangeRequestId) {
        return findByIdAndStatus(exchangeRequestId, ExchangeStatus.ACCEPTED)
            .orElseThrow(() -> new ServerException(ExceptionCode.EXCHANGE_REQUEST_NOT_FOUND));
    }
    default ExchangeRequest findByIdOrElseThrow(Long id) {
        return findById(id)
            .orElseThrow(() -> new ServerException(ExceptionCode.EXCHANGE_REQUEST_NOT_FOUND));
    }
}
