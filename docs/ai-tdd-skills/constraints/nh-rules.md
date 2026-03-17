# NH 특화 규칙 및 제약사항

> 이 문서는 **NH 도메인에 특화된 보안/데이터 보호 규칙**만 정의합니다.
> 일반 코딩 표준은 `naming-conventions.md`, `code-style.md`를 참조하세요.
> 커버리지 기준은 `test-coverage.md`를 참조하세요
>
>  **규칙 적용 우선순위: 이 문서의 규칙이 다른 규칙보다 최우선**
 
---

## 1. 개인정보 보호 (PII 마스킹)

### 1.1. 규칙

모든 개인 식별 정보(PII)는 마스킹 또는 난독화되어야 합니다.

| 데이터 유형 | 마스킹 패턴 | 예시 (원본 → 마스킹) |
|---|---|---|
| 주민등록번호 | 뒷자리 전체 마스킹 | `900101-1234567` → `900101-*******` |
| 카드번호 | 앞 12자리 마스킹 | `1234-5678-9012-3456` → `****-****-****-3456` |
| 계좌번호 | 중간 자릿수 마스킹 | `110-123-456789` → `110-***-456789` |
| 휴대폰번호 | 중간 4자리 마스킹 | `010-1234-5678` → `010-****-5678` |
| 이메일 | 아이디 부분 마스킹 | `test@nonghyupit.com` → `t***@nonghyupit.com` |

### 1.2. 테스트에서의 검증 방법

```java
// 마스킹 처리 결과 검증
@Test
@DisplayName("주민번호가 올바르게 마스킹됨")
void should_maskResidentNumber_when_validInput() {
    String result = MaskingUtil.maskResidentNumber("900101-1234567");
    assertThat(result).isEqualTo("900101-*******");
    assertThat(result).doesNotContain("1234567");   // 원본 뒷자리 미포함 확인
}
```

### 1.3. 테스트 데이터 규칙

- 테스트 코드에 **실제 개인정보 사용 금지**
- 더미 데이터 사용: `"900101-1234567"`, `"1234-5678-9012-3456"` 등 명백한 가짜 데이터
- 테스트 데이터 변수명에 `test` 접두어 사용: `testResidentNumber`, `testCardNumber`

---

## 2. 비밀번호 암호화 (Petra)

### 2.1. 규칙

모든 비밀번호 필드는 Petra 암호화를 통해 처리되어야 합니다.

| 항목 | 규칙 |
|---|---|
| 저장 | 반드시 Petra 암호화 후 저장 |
| 비교 | 평문 비교 금지, 암호화된 값으로 비교 |
| 로깅 | 비밀번호 평문 로그 출력 금지 |
| 테스트 데이터 | 하드코딩된 실제 비밀번호 사용 금지 |

### 2.2. 테스트에서의 검증 방법

```java
// 비밀번호 암호화 호출 검증
@Test
@DisplayName("사용자 생성 시 비밀번호가 Petra 암호화 됨")
void should_encryptPassword_when_creatingUser() {
    // Given
    when(passwordEncoder.encode("testPassword")).thenReturn("encryptedValue");
    // ...
  
    // Then - 암호화 메서드 호출 검증
    verify(passwordEncoder).encode("testPassword");
    // 평문 비밀번호가 직접 저장되지 않았는지 확인
    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userMapper).insert(captor.capture());
    assertThat(captor.getValue().getPassword()).isNotEqualTo("testPassword");
    assertThat(captor.getValue().getPassword()).isEqualTo("encryptedValue");
}
```

---

## 3. 감사로그 (Audit Log)

### 3.1. 규칙

핵심 비즈니스 작업에 대해 감사로그가 기록되어야 합니다.

| 감사로그 대상 | 기록 항목 |
|---|---|
| 사용자 생성/수정/삭제 | 작업자, 작업시간, 변경내용 |
| 데이터 조회 (민감정보) | 조회자, 조회 대상, 시간 등 |

### 3.2. 테스트에서의 검증 방법

```java
@Test
@DisplayName("사용자 삭제 시 감사로그가 기록됨")
void should_writeAuditLog_when_deletingUser() {
    // Given & When
    userService.deleteUser(1L);
    
    // Then - 감사로그 기록 메서드 호출 검증
    verify(userAuditLogService).createUserAuditLogAsync(any(UserAuditLog.class));
    //또는 구체적 인자 검증
    verify(userAuditLogService).createUserAuditLogAsync(argThat(
            log -> log.getApiDescription().equals("사용자 정보 삭제") && 
                    log.getUserId().equals(1L)
    ));
}
```

---

## 4. 보안 요구사항

### 4.1. 테스트 코드 보안 규칙

| 항목 | 규칙 | 위반 예시 |
|---|---|---|
| 자격증명 | 하드코딩 금지 | `password = "admin123"` |
| API 키 | 하드코딩 금지 | `apiKey = "sk-xxxxxx"` |
| DB 접속정보 | 하드코딩 금지 | "jdbc:mysql://db.."` |
| 개인정보 | 실제 데이터 금지 | `name = "홍길동"` (실존 인물) |

### 4.2. 안전한 테스트 데이터 예시

```java
// 좋은 예시 - 명백한 더미 데이터
String testPassword = "testPassword123!";
String testEmail = "test@example.com";
String testName = "테스트사용자";
String testResidentNumber = "900101-1234567";

// 나쁜 예시 - 실제 데이터로 오해될 수 있음
String password = "Nh2026!@#$";
String email = "hong@nonghyupit.com";
```

---

## 5. 규칙 요약 (에이전트 체크리스트)

| # | 체크 항목 | 검증 방법 |
|---|---|---|
| 1 | 개인정보 마스킹 로직이 있으면 마스킹 결과 검증 포함? | assertThat + 패턴 매칭 |
| 2 | 비밀번호 처리 로직이 있으면 Petra 암호화 호출 verify 포함? | verify(encoder).encode() |
| 3 | 핵심 비즈니스 로직이면 감사로그 기록 verify 포함? | verify(auditLog).log() 등 |
| 4 | 테스트 데이터에 실제 개인정보/자격증명 없음? | 더미 데이터 사용 확인 |
| 5 | 비밀번호 평문이 직접 저장/비교되지 않음? | ArgumentCaptor 검증 |