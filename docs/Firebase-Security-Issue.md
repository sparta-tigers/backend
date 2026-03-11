# Firebase Secret Management 마이그레이션 계획

## 1. 개요

현재 빌드 프로세스 및 도커 이미지 레이어에 Firebase 서비스 계정 키(`firebase-key.json`)가 정적으로 포함되는 보안 취약점이 존재합니다. 이를 제거하고 런타임 환경에서 Secret을 동적으로 주입하는 방식으로 전환합니다.

## 2. 변경 대상 및 상세 작업

### 2.1. `build.gradle` 수정

- **As-Is**: `processResources` 또는 `bootJar` 블록에서 `firebase-key.json`을 강제로 JAR에 포함.
- **To-Be**: 해당 복사 로직 제거 및 `.gitignore`에 `firebase-key.json` 추가.

### 2.2. `Dockerfile` 수정

- **As-Is**: `COPY firebase-key.json src/main/resources/` 명령어를 통해 도커 이미지 레이어에 Secret 포함.
- **To-Be**: `COPY` 명령어 삭제. 파일은 런타임에 Volume Mount로 제공.

### 2.3. `application-prod.yml` 수정

- **As-Is**: `firebase.credentials-path: classpath:firebase-key.json`
- **To-Be**: `firebase.credentials-path: ${FIREBASE_CREDENTIALS_PATH}` 환경변수 참조로 변경.

## 3. 런타임 주입 가이드

Docker 또는 Kubernetes 환경에서 다음과 같이 Secret을 주입합니다.

**Docker Compose 예시:**

```yaml
services:
  app:
    image: my-app:latest
    environment:
      - FIREBASE_CREDENTIALS_PATH=/secrets/firebase-key.json
    volumes:
      - ./secrets/firebase-key.json:/secrets/firebase-key.json:ro
```

### [코드]

마이그레이션 문서에 따른 실제 코드 수정안(Diff)입니다.

**1. `build.gradle`**

```gradle
// 하단 로직 제거 또는 주석 처리
// processResources {
//     from('firebase-key.json') {
//         into 'src/main/resources'
//     }
// }
```

**2. Dockerfile**

```Dockerfile
# 삭제 대상
# COPY firebase-key.json src/main/resources/

# 그 외 기존 빌드 로직 유지
```

**3. src/main/resources/application-prod.yml**

```YAML
firebase:
  # 변경 전: credentials-path: classpath:firebase-key.json
  # 변경 후: 외부 주입이 없을 경우 Fail-fast를 유도하기 위해 기본값 할당 금지
  credentials-path: ${FIREBASE_CREDENTIALS_PATH}
```

**4. FirebaseConfig.java (초기화 로직 점검)**

```Java
@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private String firebaseConfigPath;

    @PostConstruct
    public void init() {
        try {
            File file = new File(firebaseConfigPath);
            if (!file.exists()) {
                throw new IllegalStateException("Firebase credentials file not found at: " + firebaseConfigPath);
            }
            
            FileInputStream serviceAccount = new FileInputStream(file);
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
            throw new RuntimeException("Firebase initialization failed", e);
        }
    }
}
```

### [검증]

- Unit/Integration Test
  - FIREBASE_CREDENTIALS_PATH 환경 변수가 설정되지 않은 상태로 컨텍스트를 로드할 때 IllegalStateException 또는 IllegalArgumentException이 발생하며 Fail-fast가 정상 작동하는지 확인합니다.

- Docker Image 검증:

```Bash
docker run --rm -it <your-image> /bin/sh -c "find / -name firebase-key.json"
```

명령어를 실행하여 이미지 내에 키 파일이 존재하지 않음을 증명해야 합니다.

### [리스크]

- Git History Leak:
  - 가장 크고 시급한 위험입니다.
  - 이미 커밋된 이력이 있다면 BFG Repo-Cleaner나 git filter-repo를 사용해 히스토리에서 파일을 완전히 삭제해야 합니다.
  - 삭제하더라도 이미 복제된 포크나 로컬 저장소에는 남으므로, 기존 키 폐기 및 즉각적인 재발급이 유일한 완벽한 해결책입니다.
