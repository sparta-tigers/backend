package com.sparta.spartatigers.global.notification;

import com.sparta.spartatigers.global.notification.dto.MessagePayload;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    @Value("${notification.discord.webhook-url}")
    private String discordWebhookUrl;

    private final RestClient restClient;

    @Override
    public void send(MessagePayload payload) {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            return;
        }

        Map<String, String> message = new HashMap<>();
        message.put("content", "## " + payload.getSubject() + "\n" + "```\n" + payload.getMessage() + "\n```");

        try {
            restClient.post()
                .uri(discordWebhookUrl)
                .body(message)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("디스코드 알림 전송에 실패했습니다.", e);
        }
    }
}
