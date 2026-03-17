# 커버리지 보고서 검증

> 테스트 실행 통과 후 **커버리지가 목표 기준을 충족**하는지 검증합니다.
> 커버리지 목표 수치는 `constraints/test-coverage.md`에 정의되어 있으며,
> 이 문서는 **보고서 생성/확인/미달 시 조치 방법**에 집중합니다.

---

## 1. 검증 명령어

### 1.1. JaCoCo 커버리지 보고서 생성

```bash
./gradlew test jacocoTestReport
```

### 1.2. 커버리지 기준 검증 (자동 합격/불합격)

```bash
./gradlew jacocoTestCoverageVerification
```

### 1.3. 전체 (테스트 + 보고서 + 검증) 한 번에 실행

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

---

## 2. 보고서 위치 및 구조

### 2.1. 보고서 파일 경로

| 형식 | 경로 | 용도 |
|---|---|---|
| HTML | `build/reports/jacoco/test/html/index.html` | 시각적 확인 (브라우저) |
| XML | `build/reports/jacoco/test/jacocoTestReport.xml` | CI/CD 연동, 프로그래밍적 분석 |
| CSV | `build/reports/jacoco/test/jacocoTestReport.csv` | 데이터 추출 |

### 2.2. HTML 보고서 구조

```
index.html                      ← 전체 요약 (패키지별 커버리지)
├─ com.nhcard.al.demo.service/
│  ├─ index.html                ← 패키지 요약
│  ├─ UserService.html          ← 클래스별 상세
│  └─ UserService.java.html     ← 소스 코드 라인별 커버리지
├─ com.nhcard.al.demo.controller/
└─ ...
```

---

## 3. 합격/불합격 기준

> 상세 수치는 `constraints/test-coverage.md` 참조

### 3.1. 전체 기준 (최소)

| 항목 | 최소 기준 | 확인 방법 |
|---|---|---|
| 라인 커버리지 | **80%** | HTML 보고서 `index.html` → Total 행 |
| 분기 커버리지 | **70%** | HTML 보고서 `index.html` → Total 행 |
| 메서드 커버리지 | **100%** (public) | 클래스별 상세에서 미커버 메서드 확인 |
| 클래스 커버리지 | **100%** (대상) | 미커버 클래스 없어야 함 |

### 3.2. 계층별 기준

| 계층 | 라인 | 분기 |
|---|---|---|
| Service | 85% | 75% |
| Controller | 80% | 70% |
| Mapper | 75% | 65% |
| Utility | 90% | 85% |

---

## 4. 보고서 읽는 방법

### 4.1. HTML 보고서 색상 의미

| 색상 | 의미 | 조치 |
|---|---|---|
| **초록색** | 실행된 라인/분기 | 커버됨 (정상) |
| **빨간색** | 실행되지 않은 라인/분기 | 테스트 추가 필요 |
| **노란색** | 분기 중 일부만 실행됨 | 누락된 분기 테스트 추가 |

### 4.2. 미커버 영역 식별 방법

1. **HTML 보고서 접근**: `build/reports/jacoco/test/html/index.html`
2. **빨간색/노란색 패키지** 클릭 → 해당 패키지의 클래스 목록
3. **빨간색/노란색 클래스** 클릭 → 소스 코드 라인별 커버리지
4. 다음 항목을 확인:

| 확인 항목 | 찾는 위치 | 의미 |
|---|---|---|
| 빨간색 라인 | 소스 코드 보기 | 실행되지 않은 코드 라인 |
| 노란색 `if` 문 | 조건 분기 위치 | 일부 분기만 테스트됨 |
| 빨간색 `catch` 블록 | 예외 처리 위치 | 예외 경로 미테스트 |
| 빨간색 `else` 블록 | 조건 분기 위치 | else 경로 미테스트 |
| 빨간색 메서드 | 메서드 선언부 | 전체 메서드 미테스트 |

---

## 5. 미달 시 에이전트 조치 흐름

커버리지 기준 미달 시 다음 순서로 추가 테스트를 생성합니다.

### 5.1. 조치 순서

```
미달 확인 → 미커버 영역 식별 → 유형 판별 → 추가 테스트 생성 → 재검증
```

| 순서 | 미커버 유형 | 생성할 테스트 | 기대 효과 |
|---|---|---|---|
| 1 | **빨간색 메서드** (미테스트 public 메서드) | Level 1: Happy Case 추가 | 라인 + 메서드 커버리지 향상 |
| 2 | **노란색 if 분기** (일부만 실행) | Level 2: Edge Case 추가 | 분기 커버리지 향상 |
| 3 | **빨간색 catch/else** (예외 경로) | Level 3: Exception 테스트 추가 | 라인 + 분기 커버리지 향상 |
| 4 | **커버됨(초록) but 변이 미검출** | Level 4: Mutation 테스트 추가 | 뮤테이션 커버리지 향상 |

### 5.2. 추가 테스트 생성 예시

#### 미커버 분기 (if/else) → Edge Case 추가

```java
// 소스 코드에 다음과 같은 분기가 있고, else가 미커버인 경우:
// if (user.getStatus() == Status.ACTIVE) { ... }
// else { throw new InactiveUserException(); }

// 추가 테스트:
@Test
@DisplayName("비활성 사용자이면 관련 BusinessException 발생")
void should_throwException_when_userIsInactive() {
    // Given
    User inactiveUser = new User();
    inactiveUser.setStatus(Status.INACTIVE);
    when(userMapper.findById(anyString())).thenReturn(inactiveUser);

    // When & Then
    assertThatThrownBy(() -> userService.getUserById("testId"))
            .isInstanceOf(BusinessException.class);
}
```

#### 미커버 예외 경로 → Exception 테스트 추가

```java
// 소스 코드의 catch 블록이 미커버인 경우:
// try { userMapper.insert(user) }
// catch (DataAccessException e) { throw new UserCreateFailException(e); }

// 추가 테스트:
@Test
@DisplayName("DB 오류 시 UserCreateFailException 발생")
void should_throwException_when_dbError() {
    // Given
    when(userMapper.insert(any())).thenThrow(new DataAccessException("DB error") {});
    
    // When & Then
    assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(BusinessException.class);
}
```

### 5.3. 재검증

추가 테스트 생성 후 전체 검증을 다시 실행합니다.

```bash
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

| 재검증 결과 | 조치 |
|---|---|
| 합격 | 검증 완료 |
| 여전히 미달 | 5.1. 순서를 반복하여 추가 테스트 생성 |
| 3회 반복 후에도 미달 | 소스 코드의 테스트 불가능 영역(설정 코드 등)일 수 있음 → 제외 대상인지 확인 |

---

## 6. 뮤테이션 커버리지 검증 (선택)

> 뮤테이션 커버리지 목표 수치는 `constraints/test-coverage.md`에 정의되어 있습니다. (최소 65%)
 
### 6.1. PIT 실행 명령어

```bash
./gradlew pitest
```

### 6.2. PIT 보고서 위치

| 형식 | 경로 |
|---|---|
| HTML | `build/reports/pitest/index.html` |

### 6.3. PIT 보고서 읽는 방법

| 표시 | 의미 | 조치 |
|---|---|---|
| **KILLED** (초록) | 테스트가 변이를 감지함 | 정상 (테스트 품질 양호) |
| **SURVIVED** (빨강) | 테스트가 변이를 감지 못함 | Level 4 Mutation 테스트 추가 필요 |
| **NO_COVERAGE** (회색) | 해당 라인에 테스트 없음 | Level 1~3 테스트를 먼저 추가 |

### 6.4 SURVIVED 변이 해결 방법

SURVIVED 변이가 있으면 해당 코드에 대해 **Level 4: Mutation Testing** 테스트를 추가합니다.

```java
// 예: 조건문 변이가 SURVIVED인 경우
// 소스: if (user != null) → 변이: if (user == null)

// 추가할 테스트:
@Test
@DisplayName("사용자 존재 시 정상 처리, 미존재 시 예외 발생")
void should_handleBothCases_when_userExistsOrNot() {
    // 양쪽 분기를 모두 테스트하여 변이를 감지
}
```

> PIT 플러그인 설정 방법은 `constraints/test-coverage.md` 참조

---

## 7. 커버리지 제외 대상

다음 항목은 커버리지 측정에서 제외할 수 있습니다.

| 제외 대상 | 이유 |
|---|---|
| DTO/VO 클래스 (getter/setter) | 자동 생성 코드, 로직 없음 |
| Configuration 클래스 | 설정 코드, 비즈니스 로직 없음 |
| Application 메인 클래스 | Spring Boot 진입점 |
| Lombok 생성 코드 | 자동 생성 코드 |

> 제외 설정 방법은 `constraints/test-coverage.md` 참조
