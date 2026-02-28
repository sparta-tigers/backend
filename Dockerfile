# Build stage
FROM amazoncorretto:21-alpine AS build
WORKDIR /app

# gradle 파일 복사
COPY gradle/ gradle/
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .
COPY gradle.properties .
COPY lint.gradle .

# Firebase 키 파일을 리소스 디렉토리로 복사
COPY firebase-key.json src/main/resources/

# 소스코드 복사
COPY src/ src/

# 어플리케이션 빌드
RUN chmod +x ./gradlew
RUN ./gradlew bootJar --no-daemon

# 런타임
FROM amazoncorretto:21-alpine
WORKDIR /app

# 빌드 파일 복사
COPY --from=build /app/build/libs/*.jar app.jar

# Prod Profile
ENV SPRING_PROFILES_ACTIVE=prod

# 8080 Port open
EXPOSE 8080

# jar 시작
ENTRYPOINT ["java", "-jar", "app.jar"]