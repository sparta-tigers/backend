package com.sparta.spartatigers.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        log.info("Firebase 초기화 중");
        log.info("project id: {}", projectId);

        GoogleCredentials credentials;
        try {
            credentials = GoogleCredentials.getApplicationDefault();
            log.info("기본 자격증명으로 구글 애플리케이션을 불러왔습니다.");
        } catch (IOException e) {
            log.error("기본 자격증명으로 구글 애플리케이션을 불러오는 데 실패했습니다: {}", e.getMessage());
            throw e;
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build();

        List<FirebaseApp> firebaseApps = FirebaseApp.getApps();
        if (firebaseApps != null && !firebaseApps.isEmpty()) {
            for (FirebaseApp app : firebaseApps) {
                if (app.getName().equals(FirebaseApp.DEFAULT_APP_NAME)) {
                    log.info("Firebase가 이미 초기화되어있습니다.");
                    return app;
                }
            }
        }
        log.info("FirebaseApp가 초기화되었습니다.");

        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
