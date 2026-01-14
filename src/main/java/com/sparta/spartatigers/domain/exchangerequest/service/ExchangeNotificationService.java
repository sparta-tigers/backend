package com.sparta.spartatigers.domain.exchangerequest.service;

import com.sparta.spartatigers.domain.exchangerequest.model.ExchangeRequest;
import com.sparta.spartatigers.domain.item.model.Item;
import com.sparta.spartatigers.domain.user.model.User;
import com.sparta.spartatigers.global.firebase.FCMService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExchangeNotificationService {

    private final FCMService fcmService;

    public void sendExchangeRequested(ExchangeRequest exchangeRequest) {
        User receiver = exchangeRequest.getReceiver();
        User sender = exchangeRequest.getSender();
        Item item = exchangeRequest.getItem();

        String token = receiver.getFcmToken();
        if (token == null || token.isBlank()) {
            return;
        }

        String title = "새 교환 요청";
        String body  = String.format("'%s'에 %s님이 교환을 요청했어요.", item.getTitle(), sender.getNickname());

        fcmService.sendMessageToToken(token, title, body);
    }

    public void sendExchangeAccepted(ExchangeRequest exchangeRequest) {
        User sender = exchangeRequest.getSender();
        Item item = exchangeRequest.getItem();

        String token = sender.getFcmToken();
        if (token == null || token.isBlank()) {
            return;
        }

        String title = "교환 요청이 수락되었어요";
        String body = String.format("'%s'에 대한 교환 요청이 수락되었어요.", item.getTitle());

        fcmService.sendMessageToToken(token, title, body);
    }
}
