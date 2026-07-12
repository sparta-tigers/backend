package com.sparta.spartatigers.domain.support.chat.controller;

import com.sparta.spartatigers.domain.support.chat.model.ChatMessage;
import com.sparta.spartatigers.domain.support.chat.service.ChatService;
import com.sparta.spartatigers.global.exception.internal.WebSocketException;
import com.sparta.spartatigers.global.response.WebSocketErrorResponse;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class LiveBoardChatController {

    private final ChatService chatService;

    // 채팅
    @MessageMapping("/liveboard/send")
    public void sendMessage(ChatMessage message, Principal principal) {
        chatService.sendGroupMessage(message, principal);
    }

    // 입장
    @MessageMapping("/liveboard/enter")
    public void enterLiveBoard(
        Message<ChatMessage> message,
        Principal principal
    ) {
        chatService.enterRoom(message, principal);
    }

    // 퇴장
    @MessageMapping("/liveboard/exit")
    public void exitLiveBoard(Message<ChatMessage> message) {
        chatService.exitRoom(message);
    }

    // 예외 처리
    @MessageExceptionHandler(WebSocketException.class)
    @SendToUser("/liveboard/errors")
    public WebSocketErrorResponse handleWebSocketError(WebSocketException e) {
        return WebSocketErrorResponse.from(e.getExceptionCode());
    }
}
