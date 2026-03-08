# 데모 프로젝트 보완 리포트 (2026-03-09)

> **목적**: Gradle 6.8.3 다운그레이드 + 폐쇄망 플러그인 정합성 + 소스 품질 보완
> **환경**: JDK 1.8 + Gradle 6.8.3 + Spring Boot 2.7.17

---

## 1. 변경 요약

| 카테고리 | 변경 항목 | 수정 파일 수 |
|---------|----------|------------|
| Gradle 다운그레이드 | 7.6.4 → 6.8.3 | 3 |
| 플러그인 버전 정합성 | JaCoCo 0.8.7, PITest junit5 0.15 | 1 |
| DTO Bean Validation | 5개 DTO + 3개 Controller + Handler | 9 |
| 기타 소스 보완 | UserMapper password, Notice viewCount | 2 |
| **합계** | | **15** |

---

## 2. Gradle 6.8.3 다운그레이드

### 2.1 변경 내역

| 파일 | Before | After | 이유 |
|------|--------|-------|------|
| `gradle-wrapper.properties` | `gradle-7.6.4-bin.zip` | `gradle-6.8.3-bin.zip` | TRD v3 명시 버전 |
| `gradle-wrapper.properties` | `networkTimeout=10000` | (제거) | Gradle 7.x 전용 속성 |
| `build.gradle.kts` | `xml.required.set(true)` | `xml.isEnabled = true` | Gradle 6.x API |
| `gradle.properties` | (없음) | `org.gradle.java.home=JDK 1.8` | 개발 머신 JDK 17 대응 |

### 2.2 호환성 검증

- Gradle 6.8.3 + JDK 1.8: Spring Boot 2.7.17 공식 지원 범위
- Kotlin DSL (build.gradle.kts): Gradle 6.x 정상 지원
- 모든 플러그인 API: 6.x 호환 확인 완료

---

## 3. 폐쇄망 플러그인 정합성

### 3.1 플러그인 버전 변경

| 플러그인 | Before | After | 이유 |
|---------|--------|-------|------|
| JaCoCo toolVersion | `0.8.11` | `0.8.7` | 반입 가능 버전 |
| PITest junit5PluginVersion | `1.1.2` | `0.15` | 반입 가능 버전 |

### 3.2 아티팩트명 오류 교정

사용자가 제시한 반입 목록에서 발견된 오류:

| 항목 | 잘못된 아티팩트 | 올바른 아티팩트 |
|------|--------------|--------------|
| PITest Gradle 플러그인 | `com.github.maiflai:gradle-pitest-plugin:1.7.4` | `info.solidsoft.gradle.pitest:gradle-pitest-plugin:1.7.4` |
| PITest 코어 | `org.pitest:pitest-core:1.7.4` | `org.pitest:pitest:1.7.4` |
| PITest JUnit5 버전 | `0.15` | `0.15` (사용 가능, 권장은 `1.1.2`) |
| (누락) | - | `org.pitest:pitest-command-line:1.7.4` 추가 필요 |

### 3.3 반입 목록 문서화

`docs/closed-network-dependencies.md` 생성:
- Gradle 배포판
- 빌드 플러그인 3종 (Spring Boot, dependency-management, pitest)
- JaCoCo 아티팩트 4종 + ASM 3종
- PITest 아티팩트 4종
- 애플리케이션 의존성 전체 (Spring Boot BOM 기준)
- 테스트 의존성 전체
- 반입 체크리스트 포함

---

## 4. DTO Bean Validation 추가

### 4.1 의존성

```kotlin
implementation("org.springframework.boot:spring-boot-starter-validation")
```

Spring Boot 2.7.17이 `hibernate-validator:6.2.5.Final` + `jakarta.validation-api:2.0.2`를 관리.

### 4.2 DTO별 검증 규칙

| DTO | 필드 | 검증 |
|-----|------|------|
| `CreateUserRequest` | name | `@NotBlank` `@Size(max=100)` |
| | email | `@NotBlank` `@Email` `@Size(max=200)` |
| | password | `@NotBlank` `@Size(min=8, max=100)` |
| | phoneNumber | `@NotBlank` `@Pattern(01X + 7~8자리)` |
| `SignupRequest` | (동일) | CreateUserRequest와 동일 규칙 |
| `LoginRequest` | email | `@NotBlank` `@Email` |
| | password | `@NotBlank` |
| `CreateNoticeRequest` | title | `@NotBlank` `@Size(max=200)` |
| | content | `@NotBlank` |
| | author | `@NotBlank` `@Size(max=100)` |
| `UpdateNoticeRequest` | title | `@NotBlank` `@Size(max=200)` |
| | content | `@NotBlank` |

### 4.3 Controller @Valid 적용

| Controller | 메서드 | 적용 |
|-----------|--------|------|
| `UserController` | `createUser()`, `updateUser()` | `@Valid @RequestBody` |
| `AuthController` | `login()`, `signup()` | `@Valid @RequestBody` |
| `NoticeController` | `createNotice()`, `updateNotice()` | `@Valid @RequestBody` |

### 4.4 예외 처리

`GlobalExceptionHandler`에 `MethodArgumentNotValidException` 핸들러 추가:

```json
{
  "error": "입력값 검증에 실패했습니다",
  "fieldErrors": {
    "email": "올바른 이메일 형식이 아닙니다",
    "password": "비밀번호는 8자 이상 100자 이하여야 합니다"
  }
}
```

HTTP 상태: `400 BAD_REQUEST`

---

## 5. 기타 소스 보완

### 5.1 UserMapper.xml UPDATE에 password 추가

```xml
<!-- Before -->
SET name = #{name}, email = #{email}, phone_number = #{phoneNumber}, updated_at = CURRENT_TIMESTAMP

<!-- After -->
SET name = #{name}, email = #{email}, password = #{password}, phone_number = #{phoneNumber}, updated_at = CURRENT_TIMESTAMP
```

비밀번호 변경 시나리오 지원. Service 레이어에서 BCrypt 인코딩 후 전달 필요.

### 5.2 Notice.viewCount 기본값 초기화

```java
// Before
private int viewCount;

// After
private int viewCount = 0;
```

`new Notice()` 생성 시 명시적 초기값 보장.

---

## 6. 빌드 검증

```
./gradlew clean build → BUILD SUCCESSFUL in 29s (10 tasks)
```

- 컴파일: 성공
- 테스트 (contextLoads): 성공
- JaCoCo 리포트: 생성 완료

---

## 7. 변경 파일 목록

### 수정 (13개)

| # | 파일 | 변경 내용 |
|---|------|----------|
| 1 | `gradle/wrapper/gradle-wrapper.properties` | 6.8.3 + networkTimeout 제거 |
| 2 | `build.gradle.kts` | validation 의존성, JaCoCo 0.8.7, junit5Plugin 0.15, isEnabled API |
| 3 | `gradle.properties` | org.gradle.java.home 추가 |
| 4 | `dto/CreateUserRequest.java` | Bean Validation 어노테이션 |
| 5 | `dto/LoginRequest.java` | Bean Validation 어노테이션 |
| 6 | `dto/SignupRequest.java` | Bean Validation 어노테이션 |
| 7 | `dto/CreateNoticeRequest.java` | Bean Validation 어노테이션 |
| 8 | `dto/UpdateNoticeRequest.java` | Bean Validation 어노테이션 |
| 9 | `controller/UserController.java` | @Valid 추가 |
| 10 | `controller/AuthController.java` | @Valid 추가 |
| 11 | `controller/NoticeController.java` | @Valid 추가 |
| 12 | `exception/GlobalExceptionHandler.java` | MethodArgumentNotValidException 핸들러 |
| 13 | `mapper/UserMapper.xml` | UPDATE에 password 필드 추가 |

### 수정 (도메인)

| # | 파일 | 변경 내용 |
|---|------|----------|
| 14 | `domain/Notice.java` | viewCount = 0 초기화 |

### 신규 (2개)

| # | 파일 | 내용 |
|---|------|------|
| 15 | `docs/closed-network-dependencies.md` | 폐쇄망 반입 필요 아티팩트 전체 목록 |
| 16 | `docs/report4.md` | 본 리포트 |

### CLAUDE.md 업데이트

| # | 파일 | 변경 내용 |
|---|------|----------|
| 17 | `CLAUDE.md` | 기술 스택 버전 반영, 작업 이력 추가, 파일 경로 추가 |
