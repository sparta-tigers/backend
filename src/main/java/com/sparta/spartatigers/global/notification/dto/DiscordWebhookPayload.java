package com.sparta.spartatigers.global.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DiscordWebhookPayload {

    private final List<Embed> embeds;

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Embed {

        private final String title;
        private final String description;
        private final String url;
        private final int color;
        private final List<Field> fields;
        private final Footer footer;

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Field {
            private final String name;
            private final String value;
            private final boolean inline;
        }

        @Getter
        @RequiredArgsConstructor
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class Footer {
            private final String text;
            @JsonProperty("icon_url")
            private final String iconUrl;
        }
    }
}