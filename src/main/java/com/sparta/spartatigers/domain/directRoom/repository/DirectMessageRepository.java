package com.sparta.spartatigers.domain.directRoom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sparta.spartatigers.domain.directRoom.model.DirectMessage;

public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {

    @Query("SELECT m FROM direct_message m JOIN FETCH m.sender WHERE m.directRoom.id = :roomId")
    Page<DirectMessage> findByDirectRoomIdWithSender(
            @Param("roomId") Long roomId, Pageable pageable);

    void deleteAllByDirectRoomId(Long directRoomId);

    @Query("""
        SELECT COUNT(m)
        FROM direct_message m
        WHERE m.directRoom.id = :roomId
            AND m.sender.id <> :userId
            AND m.isRead = false
        """)
    long countUnreadMsg(@Param("roomId") Long roomId, @Param("userId")Long userId);

    @Query("""
        SELECT m
        FROM direct_message m
        WHERE m.directRoom.id = :roomId
            AND m.sender.id <> :userId
            AND m.isRead = false
        """)
    List<DirectMessage> findUnreadMsg(@Param("roomId")Long roomId, @Param("userId")Long userId);

    @Query("""
        SELECT m.directRoom.id, COUNT(m)
        FROM direct_message m
        WHERE m.directRoom.id IN :roomIds
            AND m.sender.id <> :userId
            AND m.isRead = false
        GROUP BY m.directRoom.id
        """)
    List<Object[]> countUnreadMsgInBatch(@Param("roomIds") List<Long> roomIds, @Param("userId") Long userId);

    @Query("""
        SELECT m FROM direct_message m
        JOIN FETCH m.sender
        WHERE m.directRoom.id = :roomId
            AND m.sentAt > :afterTimestamp
        ORDER BY m.sentAt ASC
        """)
    List<DirectMessage> findMessagesAfterTimestamp(
        @Param("roomId") Long roomId,
        @Param("afterTimestamp") LocalDateTime afterTimestamp,
        Pageable pageable);
}
