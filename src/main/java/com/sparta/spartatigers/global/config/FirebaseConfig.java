package com.sparta.spartatigers.global.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.credentials-path:firebase-key.json}")
    private String credentialsPath;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        // 이미 초기화된 Firebase 앱이 있는지 확인
        if (!FirebaseApp.getApps().isEmpty()) {
            FirebaseApp existingApp = FirebaseApp.getInstance();
            log.info("Firebase 앱이 이미 초기화되어 있습니다: {}", existingApp.getName());
            return existingApp;
        }
        
        log.info("Firebase 초기화 중");
        log.info("project id: {}", projectId);
        log.info("credentials path: {}", credentialsPath);

        GoogleCredentials credentials;
        try {
            // 1. 클래스패스에서 먼저 찾기 (JAR 내부 리소스)
            ClassPathResource resource = new ClassPathResource(credentialsPath);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    credentials = GoogleCredentials.fromStream(inputStream);
                    log.info("클래스패스에서 Firebase 크레덴셜을 불러왔습니다: {}", credentialsPath);
                }
            } else {
                // 2. 클래스패스에 없으면 파일 시스템에서 찾기
                File credentialsFile = new File(credentialsPath);
                if (!credentialsFile.exists()) {
                    throw new IOException("Firebase 크레덴셜 파일을 찾을 수 없습니다. 클래스패스와 파일 시스템 모두에서 찾지 못함: " + credentialsPath);
                }
                
                try (FileInputStream fileInputStream = new FileInputStream(credentialsFile)) {
                    credentials = GoogleCredentials.fromStream(fileInputStream);
                    log.info("파일 시스템에서 Firebase 크레덴셜을 불러왔습니다: {}", credentialsFile.getAbsolutePath());
                }
            }
        } catch (IOException e) {
            log.error("Firebase 크레덴셜을 불러오는 데 실패했습니다: {}", e.getMessage());
            throw e;
        }

        FirebaseOptions options = FirebaseOptions.builder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("FirebaseApp가 초기화되었습니다.");

        return app;
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
