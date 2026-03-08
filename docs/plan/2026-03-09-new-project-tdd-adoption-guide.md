# 신규 프로젝트 TDD 도입 절차 가이드

> **목적**: AI TDD 스킬 문서를 활용하여 신규(클린) 프로젝트에 Red-Green-Refactor TDD를 도입하는 절차
> **전제**: 현재 데모 프로젝트(ai-tdd-demo)는 레거시 프로젝트 대상 "테스트 후행 생성"을 검증하는 사전 작업이며, 본 문서는 신규 프로젝트에서 "테스트 선행 작성(TDD)"을 적용하는 방법을 다룬다.

---

## 레거시 vs 신규 프로젝트: 근본적 차이

| 구분 | 레거시 (현재 데모) | 신규 프로젝트 (본 문서 대상) |
|------|-------------------|---------------------------|
| 코드 존재 여부 | 이미 구현됨 | 아직 없음 |
| 테스트 작성 시점 | 구현 후 (Test-After) | 구현 전 (Test-First) |
| AI 에이전트 역할 | 소스 분석 → 테스트 생성 | 요구사항 분석 → 테스트 생성 → 구현 가이드 |
| Red-Green-Refactor | 적용 불가 (이미 Green 상태) | **핵심 사이클** |
| 설계 영향 | 없음 (기존 설계 따름) | 테스트가 설계를 주도 |

---

## 1단계: 프로젝트 초기 설정

### 1.1 프로젝트 스캐폴딩

```
신규 프로젝트 생성
├── Spring Initializr (또는 기존 아키타입)
├── 기술 스택 확정 (JDK, Spring Boot, DB, 빌드 도구)
├── 패키지 구조 설계 (controller / service / mapper / domain / dto / exception)
└── 빌드 설정 (build.gradle.kts)
```

### 1.2 AI TDD 환경 구성

신규 프로젝트 루트에 AI TDD 문서를 배치한다.

```
{new-project}/
├── docs/
│   └── ai-tdd/
│       ├── ai-tdd-agent/
│       │   ├── SKILL.md              ← 에이전트 행동 지시서
│       │   └── batch-execution.md    ← 배치 실행 가이드
│       ├── ai-tdd-review-agent/
│       │   └── SKILL.md              ← 리뷰 에이전트
│       └── ai-tdd-skills/
│           ├── .claude.md            ← 프로젝트별 설정 (★ 수정 필요)
│           ├── SKILL.md              ← 테스트 생성 가이드
│           ├── templates/            ← 레이어별 템플릿
│           ├── constraints/          ← 제약조건 (NH 규칙 등)
│           ├── references/examples/  ← 참조 예제
│           └── verification/         ← 검증 절차
├── CLAUDE.md                         ← Claude 프로젝트 컨텍스트 (★ 수정 필요)
└── build.gradle.kts
```

### 1.3 `.claude.md` 커스터마이징

신규 프로젝트의 기술 스택에 맞게 수정한다.

```yaml
# 필수 수정 항목
base_package: com.{조직}.{프로젝트}    # 예: com.nhcard.al.tt
java_version: "1.8"                    # 또는 "17"
spring_boot_version: "2.7.17"          # 또는 "3.x"
build_tool: "gradle"                   # 또는 "maven"
test_framework: "junit5"
mock_framework: "mockito"
assertion_library: "assertj"
coverage_tool: "jacoco"
mutation_tool: "pitest"
```

### 1.4 품질 도구 설정

`build.gradle.kts`에 품질 게이트를 사전 설정한다.

```kotlin
// JaCoCo 커버리지 임계값
jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit { minimum = "0.80".toBigDecimal() }           // 라인 80%
            limit { counter = "BRANCH"; minimum = "0.70".toBigDecimal() } // 분기 70%
        }
    }
}

// PITest 뮤테이션 테스트
pitest {
    targetClasses.set(listOf("com.{조직}.{프로젝트}.*"))
    mutationThreshold.set(65)
}
```

---

## 2단계: 요구사항 → 테스트 케이스 설계

### 2.1 요구사항 분석

신규 프로젝트에서는 소스 코드가 없으므로, **요구사항 문서**가 테스트 설계의 입력이 된다.

```
입력 소스 (레거시와 다른 점)
├── 레거시: Java 소스 코드 → 메서드 시그니처 분석
└── 신규:  요구사항/설계 문서 → 인터페이스 설계 → 테스트 설계
```

**요구사항에서 추출할 항목:**

| 추출 항목 | 예시 |
|-----------|------|
| 도메인 엔티티 | User, Notice, Account |
| 비즈니스 규칙 | "이메일 중복 불가", "비밀번호 8자 이상" |
| API 엔드포인트 | POST /api/users, GET /api/users/{id} |
| 예외 상황 | 사용자 미존재, 인증 실패, 권한 부족 |
| 보안 요건 | 비밀번호 암호화, 개인정보 마스킹 |

### 2.2 인터페이스 선(先) 설계

테스트를 먼저 작성하려면, **구현은 없지만 인터페이스(시그니처)는 있어야** 한다.

```java
// Step 1: 인터페이스/시그니처만 선언 (구현 없음)
public class UserService {
    public User createUser(CreateUserRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public User getUserById(Long id) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
```

이 단계에서 결정하는 것:
- 메서드 이름과 파라미터 타입
- 반환 타입
- 발생 가능한 예외 타입
- 의존성 (어떤 Mapper, 어떤 Service를 주입받을지)

### 2.3 테스트 케이스 목록 작성

4-Level 체계로 테스트 케이스를 사전에 설계한다.

```
UserService 테스트 케이스 목록 (공식: max(10, 4메서드 × 3) = 12개)

Level 1 - Happy Case (4개, 메서드당 1개)
  ├── 유효한 요청으로 사용자 생성 성공
  ├── 정상 ID로 사용자 조회 성공
  ├── 전체 사용자 목록 조회 성공
  └── 사용자 정보 수정 성공

Level 2 - Edge Case (2개, 나머지 할당)
  ├── ID가 null이면 예외 발생
  └── 이메일이 빈 문자열이면 예외 발생

Level 3 - Exception (4개, throw 문 기반)
  ├── 중복 이메일이면 DuplicateEmailException 발생
  ├── 존재하지 않는 ID 조회 시 UserNotFoundException 발생
  ├── 존재하지 않는 ID 수정 시 UserNotFoundException 발생
  └── 존재하지 않는 ID 삭제 시 UserNotFoundException 발생

Level 4 - Mutation (2개, 최소 2개)
  ├── 사용자 생성 시 비밀번호 인코딩이 반드시 호출됨
  └── 이메일 중복 확인이 insert보다 먼저 호출됨
```

---

## 3단계: Red-Green-Refactor 사이클 실행

### 3.1 전체 흐름

```
┌─────────────────────────────────────────────────────────┐
│                  Red-Green-Refactor 사이클                │
│                                                          │
│  ┌──────┐     ┌──────┐     ┌──────────┐                │
│  │ RED  │────▶│GREEN │────▶│REFACTOR  │──┐              │
│  │      │     │      │     │          │  │              │
│  │테스트 │     │최소  │     │설계 개선 │  │              │
│  │작성  │     │구현  │     │중복 제거 │  │              │
│  │(실패)│     │(통과)│     │클린 코드 │  │              │
│  └──────┘     └──────┘     └──────────┘  │              │
│      ▲                                    │              │
│      └────────────────────────────────────┘              │
│                 다음 테스트로 반복                         │
└─────────────────────────────────────────────────────────┘
```

### 3.2 RED 단계 — 실패하는 테스트 작성

**목표: 컴파일은 되지만 실행하면 실패하는 테스트를 작성한다.**

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    // ── Level 1: Happy Case ──

    @Test
    @DisplayName("유효한 요청으로 사용자 생성 성공")
    void should_create_user_when_valid_request() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("테스트사용자");
        request.setEmail("test@example.com");
        request.setPassword("testPassword123");
        request.setPhoneNumber("01012345678");

        when(userMapper.findByEmail("test@example.com")).thenReturn(null);
        when(passwordEncoder.encode("testPassword123")).thenReturn("encodedPassword");

        // When
        User result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(passwordEncoder).encode("testPassword123");
        verify(userMapper).insert(any(User.class));
    }
}
```

**RED 단계 검증:**

```bash
./gradlew test --tests "com.example.service.UserServiceTest"
# 결과: FAILED (UnsupportedOperationException 또는 assertion 실패)
# 이것이 정상 — 아직 구현이 없으므로 실패해야 함
```

> **핵심 원칙**: RED 단계에서 테스트가 **올바른 이유로** 실패하는지 확인한다.
> - `UnsupportedOperationException` → 구현이 없어서 실패 (정상)
> - `NullPointerException` → Mock 설정 오류 (테스트 수정 필요)
> - 컴파일 에러 → 인터페이스 정의 불완전 (인터페이스 수정 필요)

### 3.3 GREEN 단계 — 최소 구현으로 통과시키기

**목표: 테스트를 통과시키는 최소한의 코드만 작성한다.**

```java
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        // 최소 구현: 테스트가 요구하는 것만 구현
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());

        userMapper.insert(user);
        return user;
    }
}
```

**GREEN 단계 검증:**

```bash
./gradlew test --tests "com.example.service.UserServiceTest"
# 결과: PASSED
# 모든 테스트가 통과해야 함
```

> **핵심 원칙**: "최소 구현"이란 테스트를 통과시키기 위해 **필요한 것만** 작성하는 것이다.
> - 아직 작성하지 않은 테스트의 로직을 미리 구현하지 않는다
> - "나중에 필요할 것 같은" 코드를 추가하지 않는다 (YAGNI)

### 3.4 REFACTOR 단계 — 설계 개선

**목표: 테스트는 그대로 통과하면서 코드 품질을 개선한다.**

```
리팩토링 체크리스트:
├── 중복 코드 제거
├── 메서드 추출 (긴 메서드 분리)
├── 변수명/메서드명 개선
├── 불필요한 코드 제거
├── 디자인 패턴 적용 (필요시)
└── 테스트 코드도 리팩토링 (중복 setup 추출 등)
```

**REFACTOR 단계 검증:**

```bash
./gradlew test --tests "com.example.service.UserServiceTest"
# 결과: PASSED (리팩토링 후에도 모든 테스트 통과 필수)
```

> **핵심 원칙**: 리팩토링 중에 새로운 기능을 추가하지 않는다.
> 리팩토링은 "동작을 변경하지 않으면서 구조를 개선"하는 것이다.

### 3.5 사이클 반복

하나의 테스트 케이스에 대해 Red → Green → Refactor를 완료한 후, 다음 테스트로 넘어간다.

```
반복 순서 (권장):

1차 사이클: Happy Case 1개
   RED   → should_create_user_when_valid_request (실패)
   GREEN → createUser() 최소 구현 (통과)
   REFACTOR → 코드 정리

2차 사이클: 같은 메서드의 Exception Case
   RED   → should_throw_DuplicateEmailException_when_email_exists (실패)
   GREEN → 중복 이메일 검사 로직 추가 (통과)
   REFACTOR → 코드 정리

3차 사이클: 다음 메서드의 Happy Case
   RED   → should_return_user_when_valid_id (실패)
   GREEN → getUserById() 구현 (통과)
   REFACTOR → 코드 정리

... 12개 테스트 완료까지 반복
```

---

## 4단계: 레이어별 TDD 적용 순서

### 4.1 의존성 기반 Bottom-Up 순서

```
실행 순서 (위상 정렬):

Phase 1: 의존성 없는 계층
├── Domain (엔티티 클래스)
├── DTO (요청/응답 객체)
├── Exception (커스텀 예외)
└── Utility (static 유틸)

Phase 2: DB 계층
└── Mapper (MyBatis 인터페이스 + XML)

Phase 3: 비즈니스 계층
└── Service (Mapper 의존)

Phase 4: 표현 계층
└── Controller (Service 의존)

Phase 5: 횡단 관심사
├── Security (JWT, Filter)
└── Exception Handler
```

### 4.2 레이어별 TDD 특성

#### Domain / DTO — 단순 POJO, TDD 불필요

```
도메인/DTO는 단순 데이터 홀더이므로 별도 TDD 사이클 없이 생성한다.
단, Bean Validation 어노테이션이 있다면 검증 테스트를 작성한다.
```

#### Utility — 순수 단위 테스트, TDD 최적

```
Utility는 외부 의존성이 없어 TDD에 가장 적합하다.

RED:   MaskingUtil.maskEmail("test@example.com") → "t***@example.com" 테스트 작성
GREEN: maskEmail() 구현
REFACTOR: 정규식 최적화, 공통 로직 추출
```

#### Mapper — Mock 기반 또는 @MybatisTest

```
Mock 기반 (권장): Service 테스트에서 간접 검증
DB 기반 (@MybatisTest): SQL 정확성이 중요할 때

RED:   userMapper.findByEmail("test@example.com") → User 반환 테스트
GREEN: UserMapper.xml에 SELECT 쿼리 작성
REFACTOR: SQL 최적화
```

#### Service — Mock 기반, TDD 핵심 대상

```
Service는 비즈니스 로직의 핵심이므로 TDD의 주 대상이다.

RED:   userService.createUser(request) → User 반환 + verify(mapper) 테스트
GREEN: createUser() 메서드 구현
REFACTOR: 비즈니스 로직 분리, 검증 로직 추출
```

#### Controller — MockMvc 기반 슬라이스 테스트

```
Controller는 HTTP 요청/응답 변환에 집중한다.

RED:   POST /api/users (JSON) → 201 Created + User JSON 테스트
GREEN: @PostMapping createUser() 구현
REFACTOR: 응답 형식 통일, 공통 예외 처리
```

---

## 5단계: AI 에이전트와 TDD 사이클 통합

### 5.1 AI 에이전트의 역할 변화

```
레거시 모드 (현재 데모):
  소스 분석 → 테스트 생성 → 검증
  (소스가 이미 있으므로 분석이 입력)

TDD 모드 (신규 프로젝트):
  요구사항 분석 → 인터페이스 설계 → RED 테스트 생성 → GREEN 구현 가이드 → REFACTOR 제안
  (요구사항이 입력, 소스는 출력)
```

### 5.2 AI 에이전트 활용 시나리오

#### 시나리오 A: AI가 테스트만 생성, 개발자가 구현

```
1. 개발자: 요구사항 + 인터페이스 시그니처 제공
2. AI 에이전트: RED 테스트 코드 생성 (4-Level, Given-When-Then)
3. 개발자: GREEN 구현 코드 작성
4. AI 리뷰 에이전트: 테스트 품질 검증
5. 개발자: REFACTOR
```

**장점**: 개발자가 구현을 직접 하므로 설계 이해도가 높음
**적합**: TDD 경험이 적은 팀에 도입 초기

#### 시나리오 B: AI가 전체 사이클 수행

```
1. 개발자: 요구사항 제공
2. AI 에이전트:
   a. 인터페이스 설계 제안 → 개발자 승인
   b. RED 테스트 생성
   c. GREEN 최소 구현 생성
   d. REFACTOR 제안
3. AI 리뷰 에이전트: 품질 검증
4. 개발자: 코드 리뷰 + 승인
```

**장점**: 생산성 극대화
**적합**: TDD 경험이 있고 AI 결과물을 리뷰할 수 있는 팀

#### 시나리오 C: AI 배치 생성 + 개발자 구현 (권장)

```
1. 개발자: 도메인 설계 + 인터페이스 시그니처 확정
2. AI 에이전트: 배치로 전체 테스트 스위트 생성 (batch-execution.md 활용)
   - 위상 정렬 순서: Util → Mapper → Service → Controller
   - 클래스당 max(10, methods × 3)개 테스트
3. 개발자: 테스트 리뷰 + 불필요한 테스트 제거/수정
4. 개발자: 테스트 기반으로 구현 (자연스러운 TDD 흐름)
5. AI 리뷰 에이전트: 품질 검증
```

**장점**: 대규모 프로젝트에서 테스트 설계 시간 대폭 단축
**적합**: 프로젝트 초기 구조가 확정된 상태

### 5.3 기존 스킬 문서 재활용 방법

| 기존 문서 | 레거시 용도 | 신규 TDD 용도 |
|-----------|------------|--------------|
| `ai-tdd-agent/SKILL.md` | 소스 분석 → 테스트 생성 | 인터페이스 분석 → RED 테스트 생성 |
| `ai-tdd-skills/GENERATION-GUIDE.md` | 4-Level 생성 규칙 | **그대로 사용** (테스트 구조 동일) |
| `templates/*.md` | 레이어별 테스트 구조 | **그대로 사용** |
| `constraints/nh-rules.md` | NH 보안 규칙 적용 | **그대로 사용** (최우선 적용) |
| `references/examples/*.md` | 참조 예제 | **그대로 사용** |
| `verification/*.md` | 컴파일/실행/커버리지 검증 | **그대로 사용** |
| `ai-tdd-review-agent/SKILL.md` | 생성 결과 품질 검증 | **그대로 사용** |
| `batch-execution.md` | 다중 클래스 일괄 생성 | **그대로 사용** |

> **핵심**: 테스트 생성 규칙, 템플릿, 제약조건, 검증 절차는 레거시/신규 모두 동일하게 적용된다.
> 차이는 **입력이 소스 코드냐 요구사항이냐**뿐이다.

---

## 6단계: 품질 게이트와 완료 기준

### 6.1 단계별 품질 게이트

```
Gate 1: RED 검증
├── 테스트가 컴파일되는가? → ./gradlew compileTestJava
├── 테스트가 실패하는가? → ./gradlew test (FAILED 확인)
└── 올바른 이유로 실패하는가? (UnsupportedOperationException 등)

Gate 2: GREEN 검증
├── 모든 테스트가 통과하는가? → ./gradlew test (PASSED)
├── 기존 테스트가 깨지지 않았는가? → 전체 테스트 실행
└── 최소 구현인가? (불필요한 코드 없는지 확인)

Gate 3: REFACTOR 검증
├── 리팩토링 후에도 모든 테스트 통과? → ./gradlew test
├── 코드 품질 개선되었는가? (중복 제거, 명명 개선)
└── 새로운 기능이 추가되지 않았는가?

Gate 4: 커버리지 검증
├── 라인 커버리지 ≥ 80%? → ./gradlew jacocoTestCoverageVerification
├── 분기 커버리지 ≥ 70%?
└── 뮤테이션 스코어 ≥ 65%? → ./gradlew pitest
```

### 6.2 완료 기준 (Definition of Done)

```
기능 단위 완료 기준:
□ 모든 테스트 케이스 작성 완료 (max(10, methods × 3)개 이상)
□ 4-Level 분포 충족 (Happy 40% / Edge 30% / Exception 20% / Mutation 10%)
□ Given-When-Then 패턴 준수
□ NH 보안 규칙 준수 (PII 마스킹, 비밀번호 암호화 검증)
□ 테스트 더미 데이터만 사용 (실제 개인정보 금지)
□ JaCoCo 임계값 통과 (라인 80%, 분기 70%)
□ PITest 뮤테이션 스코어 65% 이상
□ AI 리뷰 에이전트 점수 70점 이상 (WARN 이상)
□ 코드 리뷰 완료
```

---

## 7단계: 실전 도입 로드맵

### Phase 1: 파일럿 (2주)

```
목표: TDD 사이클에 익숙해지기
범위: 1개 도메인 (예: User CRUD)

Week 1:
├── 프로젝트 설정 + AI TDD 문서 배치
├── 인터페이스 설계 (User 도메인)
├── MaskingUtil TDD 사이클 (가장 단순한 대상)
└── UserMapper TDD 사이클

Week 2:
├── UserService TDD 사이클 (핵심 대상)
├── UserController TDD 사이클
├── 커버리지/뮤테이션 검증
└── 회고: 문서 보정점 도출
```

### Phase 2: 확대 (2주)

```
목표: 배치 생성 + 다중 도메인
범위: 2~3개 도메인 추가

Week 3:
├── AI 에이전트 배치 실행으로 테스트 스위트 생성
├── 생성된 테스트 리뷰 + 수정
├── 테스트 기반 구현 시작
└── AI 리뷰 에이전트로 품질 검증

Week 4:
├── 잔여 도메인 구현
├── 통합 테스트 추가
├── 전체 커버리지 목표 달성
└── 문서 피드백 → 스킬 문서 업데이트
```

### Phase 3: 정착 (지속)

```
목표: 팀 표준으로 정착
활동:
├── 새 기능 개발 시 TDD 사이클 기본 적용
├── PR 리뷰 시 테스트 선행 여부 확인
├── AI 에이전트 활용 비율 점진 확대
├── 커버리지/뮤테이션 CI 자동 검증
└── 성과 리포트 주기적 생성 (performance-report.md 활용)
```

---

## 부록 A: Red-Green-Refactor 체크리스트

개발자가 매 사이클마다 확인하는 체크리스트.

```
□ RED
  □ 테스트 1개 작성
  □ ./gradlew compileTestJava 통과 (컴파일 성공)
  □ ./gradlew test 실패 확인 (올바른 이유로 실패)

□ GREEN
  □ 최소 코드로 구현
  □ ./gradlew test 통과 확인
  □ 불필요한 코드 추가하지 않았는지 확인

□ REFACTOR
  □ 중복 코드 제거
  □ 변수명/메서드명 개선
  □ ./gradlew test 통과 확인 (리팩토링 후 재검증)
  □ 다음 테스트로 이동
```

---

## 부록 B: 자주 하는 실수와 대응

| 실수 | 증상 | 대응 |
|------|------|------|
| 테스트 없이 구현 먼저 작성 | GREEN 상태에서 시작 → RED 단계 건너뜀 | 구현 삭제 후 테스트부터 다시 시작 |
| 한 번에 많은 테스트 작성 | 여러 테스트가 동시에 RED → GREEN 구현이 커짐 | 1개씩 작성하고 사이클 완료 후 다음으로 |
| GREEN에서 과도한 구현 | 현재 테스트에 불필요한 로직까지 구현 | "이 테스트를 통과시키는 데 필요한가?" 질문 |
| REFACTOR 건너뛰기 | 기술 부채 누적 | 매 3~5 사이클마다 반드시 리팩토링 시간 확보 |
| 테스트 코드 리팩토링 안 함 | 테스트 코드도 중복/가독성 저하 | @BeforeEach, 헬퍼 메서드, @ParameterizedTest 활용 |
| Mock 과다 사용 | 테스트가 구현에 강하게 결합 | 행위 검증보다 상태 검증 우선, Mock은 외부 의존성만 |

---

## 부록 C: 레거시 데모 → 신규 TDD 전환 시 문서 수정 포인트

현재 데모 프로젝트의 AI TDD 문서를 신규 프로젝트에 적용할 때 수정이 필요한 부분.

| 문서 | 수정 포인트 |
|------|------------|
| `ai-tdd-agent/SKILL.md` | Stage 2 소스 분석 → 인터페이스/요구사항 분석으로 변경 |
| `ai-tdd-skills/.claude.md` | base_package, 버전 정보 프로젝트에 맞게 수정 |
| `constraints/nh-rules.md` | 조직별 보안 규칙으로 커스터마이징 |
| `templates/*.md` | 프레임워크 버전에 따른 import 경로 수정 (javax ↔ jakarta) |
| `verification/*.md` | 빌드 도구 명령어 수정 (Gradle ↔ Maven) |

> **수정 불필요한 것**: 4-Level 구조, Given-When-Then 패턴, 테스트 수 공식, 안티패턴 목록, 리뷰 점수 체계 — 이들은 프로젝트에 무관하게 보편 적용된다.
