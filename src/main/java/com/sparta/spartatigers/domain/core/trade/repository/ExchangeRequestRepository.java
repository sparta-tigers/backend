package com.sparta.spartatigers.domain.core.trade.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeRequest;
import com.sparta.spartatigers.domain.core.trade.model.ExchangeStatus;
import com.sparta.spartatigers.global.exception.enums.ExceptionCode;
import com.sparta.spartatigers.global.exception.internal.ServerException;

public interface ExchangeRequestRepository extends JpaRepository<ExchangeRequest, Long> {

    boolean existsBySenderIdAndReceiverIdAndItemId(Long senderId, Long receiverId, Long itemId);

    @EntityGraph(attributePaths = { "receiver", "sender", "item" })
    @Query("select e from exchange_request e where e.receiver.id = :receiverId")
    Page<ExchangeRequest> findAllReceiveRequest(@Param("receiverId") Long receiverId, Pageable pageable);

    @EntityGraph(attributePaths = { "receiver", "sender", "item" })
    @Query("select e from exchange_request e where e.sender.id = :senderId")
    Page<ExchangeRequest> findAllSentRequest(@Param("senderId") Long senderId, Pageable pageable);

    @EntityGraph(attributePaths = { "receiver", "sender", "item" })
    @Query("select e from exchange_request e where e.sender.id = :senderId and e.status = :status")
    Page<ExchangeRequest> findAllSentRequestWithStatus(@Param("senderId") Long senderId,
            @Param("status") ExchangeStatus status, Pageable pageable);

    @EntityGraph(attributePaths = { "receiver", "sender", "item" })
    @Query("select e from exchange_request e where e.receiver.id = :receiverId and e.status = :status")
    Page<ExchangeRequest> findAllReceiveRequestWithStatus(@Param("receiverId") Long receiverId,
            @Param("status") ExchangeStatus status, Pageable pageable);

    Optional<ExchangeRequest> findByIdAndStatus(
            Long exchangeRequestId, ExchangeStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM exchange_request e WHERE e.id = :id")
    Optional<ExchangeRequest> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM exchange_request e WHERE e.item.id = :itemId AND e.status = :status")
    List<ExchangeRequest> findByItemIdAndStatusForUpdate(@Param("itemId") Long itemId,
            @Param("status") ExchangeStatus status);

    List<ExchangeRequest> findByItemIdAndStatus(
            Long itemId, ExchangeStatus status);

    List<ExchangeRequest> findByItemIdAndStatusIn(
            Long itemId, List<ExchangeStatus> statuses);

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
