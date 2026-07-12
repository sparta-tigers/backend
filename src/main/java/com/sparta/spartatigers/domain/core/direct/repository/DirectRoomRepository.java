package com.sparta.spartatigers.domain.core.direct.repository;

import com.sparta.spartatigers.domain.core.direct.model.DirectRoom;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DirectRoomRepository extends JpaRepository<DirectRoom, Long> {
    Optional<DirectRoom> findByExchangeRequestId(Long exchangeRequestId);

    @Query(
        value = "select dr from direct_rooms dr " +
            "join fetch dr.sender s " +
            "join fetch dr.receiver r " +
            "where dr.sender.id = :userId or dr.receiver.id = :userId",
        countQuery = "select count(dr) from direct_rooms dr " +
            "where dr.sender.id = :userId or dr.receiver.id = :userId"
    )
    Page<DirectRoom> findBySenderIdOrReceiverIdWithUsersAndItem(
        Long userId,
        Pageable pageable
    );

    @Query(
        "SELECT dr FROM direct_rooms dr WHERE dr.exchangeRequestId IN :exchangeRequestIds"
    )
    List<DirectRoom> findByExchangeRequestIdIn(
        @Param("exchangeRequestIds") List<Long> exchangeRequestIds
    );

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select dr from direct_rooms dr where dr.id = :id")
    Optional<DirectRoom> findByIdWithLock(Long id);
}
