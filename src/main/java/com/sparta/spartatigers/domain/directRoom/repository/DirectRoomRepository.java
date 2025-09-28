package com.sparta.spartatigers.domain.directRoom.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.sparta.spartatigers.domain.directRoom.model.DirectRoom;
import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;

public interface DirectRoomRepository extends JpaRepository<DirectRoom, Long> {

    Optional<DirectRoom> findByExchangeRequest(ExchangeRequest exchangeRequest);

    @Query(
        value =
            "select dr from direct_rooms dr "
                + "join fetch dr.sender s "
                + "join fetch dr.receiver r "
                + "join fetch dr.exchangeRequest er "
                + "join fetch er.item i "
                + "where dr.sender.id = :userId or dr.receiver.id = :userId",
        countQuery =
            "select count(dr) from direct_rooms dr "
                + "where dr.sender.id = :userId or dr.receiver.id = :userId")
    Page<DirectRoom> findBySenderIdOrReceiverIdWithUsersAndItem(Long userId, Pageable pageable);

    Optional<DirectRoom> findByExchangeRequestId(Long exchangeRequestId);

    boolean existsBySenderIdAndReceiverId(Long senderId, Long receiverId);
}
