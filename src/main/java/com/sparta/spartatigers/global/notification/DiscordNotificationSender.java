package com.sparta.spartatigers.global.notification;

import com.sparta.spartatigers.global.notification.dto.AlertLevel;
import com.sparta.spartatigers.global.notification.dto.DiscordWebhookPayload;
import com.sparta.spartatigers.global.notification.dto.DiscordWebhookPayload.Embed;
import com.sparta.spartatigers.global.notification.dto.MessagePayload;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiscordNotificationSender implements NotificationSender {

    @Value("${discord.webhook-url}")
    private String discordWebhookUrl;

    private final RestClient restClient;

    @Override
    public void send(MessagePayload payload) {
        if (discordWebhookUrl == null || discordWebhookUrl.isEmpty()) {
            return;
        }

        DiscordWebhookPayload discordPayload = createDiscordMessage(payload);

        try {
            restClient
                .post()
                .uri(discordWebhookUrl)
                .body(discordPayload)
                .retrieve()
                .toBodilessEntity();
        } catch (Exception e) {
            log.error("디스코드 알림 전송에 실패했습니다.", e);
        }
    }

    private DiscordWebhookPayload createDiscordMessage(MessagePayload payload) {
        Embed embed = Embed.builder()
            .title(payload.getSubject())
            .description(payload.getMessage())
            .color(getColorForLevel(payload.getLevel()))
            .url(payload.getBacklink())
            .fields(
                (payload.getMetadata() != null)
                    ? payload
                          .getMetadata()
                          .entrySet()
                          .stream()
                          .map(entry ->
                              new DiscordWebhookPayload.Embed.Field(
                                  entry.getKey(),
                                  entry.getValue(),
                                  true
                              )
                          )
                          .collect(Collectors.toList())
                    : null
            )
            .build();

        return new DiscordWebhookPayload(List.of(embed));
    }

    private int getColorForLevel(AlertLevel level) {
        return switch (level) {
            case INFO -> 3447003; // Blue
            case WARN -> 15105570; // Yellow
            case ERROR -> 15158332; // Red
            case CRITICAL -> 10038562; // Dark Red
        };
    }
}
