package com.sparta.spartatigers.global.notification;

import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    @Value("${notification.discord.webhook-url}")
    private String discordWebhookUrl;

    private final RestTemplate restTemplate;

    @Override
    public void send(String subject, String content) {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            return;
        }

        Map<String, String> message = new HashMap<>();
        message.put("content", "## " + subject + "\n" + "```\n" + content + "\n```");

        try {
            restTemplate.postForObject(discordWebhookUrl, message, String.class);
        } catch (Exception e) {
            log.error("디스코드 알림 전송에 실패했습니다.", e);
        }
    }
}
