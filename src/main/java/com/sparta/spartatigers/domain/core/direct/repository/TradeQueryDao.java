package com.sparta.spartatigers.domain.core.direct.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TradeQueryDao {

    private final JdbcTemplate jdbcTemplate;

    public TradeItemInfo getTradeItemInfo(Long exchangeRequestId) {
        String sql =
            "SELECT i.title, i.category, i.image " +
            "FROM exchange_request e " +
            "JOIN item i ON e.item_id = i.id " +
            "WHERE e.id = ?";

        return jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) ->
                new TradeItemInfo(
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("image")
                ),
            exchangeRequestId
        );
    }

    public ExchangeUsersInfo getExchangeUsers(Long exchangeRequestId) {
        String sql =
            "SELECT sender_id, receiver_id FROM exchange_request WHERE id = ?";
        return jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) ->
                new ExchangeUsersInfo(
                    rs.getLong("sender_id"),
                    rs.getLong("receiver_id")
                ),
            exchangeRequestId
        );
    }

    public String getExchangeStatus(Long exchangeRequestId) {
        String sql = "SELECT status FROM exchange_request WHERE id = ?";
        return jdbcTemplate.queryForObject(
            sql,
            String.class,
            exchangeRequestId
        );
    }

    public TradeItemDetailInfo getTradeItemDetail(Long exchangeRequestId) {
        String sql =
            "SELECT i.id, i.title, i.description, i.category, i.status, u.id as owner_id, u.nickname as owner_nickname, e.status as exchange_status, e.sender_id, e.receiver_id " +
            "FROM exchange_request e " +
            "JOIN item i ON e.item_id = i.id " +
            "JOIN users u ON i.user_id = u.id " +
            "WHERE e.id = ?";
        return jdbcTemplate.queryForObject(
            sql,
            (rs, rowNum) ->
                new TradeItemDetailInfo(
                    rs.getLong("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("status"),
                    rs.getLong("owner_id"),
                    rs.getString("owner_nickname"),
                    rs.getString("exchange_status"),
                    rs.getLong("sender_id"),
                    rs.getLong("receiver_id")
                ),
            exchangeRequestId
        );
    }

    public record TradeItemInfo(String title, String category, String image) {}

    public record ExchangeUsersInfo(Long senderId, Long receiverId) {}

    public record TradeItemDetailInfo(
        Long itemId,
        String title,
        String description,
        String category,
        String status,
        Long ownerId,
        String ownerNickname,
        String exchangeStatus,
        Long senderId,
        Long receiverId
    ) {}
}
