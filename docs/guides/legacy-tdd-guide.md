# 레거시 프로젝트 AI TDD 도입 가이드

> **대상**: 이미 구현된 프로젝트에 AI 에이전트를 활용하여 테스트 코드를 생성하고, TDD 문화를 도입하려는 개발자
>
> **전제**: Claude Code 설치 완료, 폐쇄망 환경

---

## 1. Why — 왜 테스트 코드가 필요한가

### 1.1. 레거시 프로젝트의 현실

레거시 프로젝트에서 가장 많이 듣는 말:

> "이거 수정하면 다른 데 영향 없을까요?"

테스트 코드가 없는 프로젝트에서는 다음과 같은 문제가 반복됩니다.

| 문제 | 원인 | 결과 |
|---|---|---|
| **변경 공포** | 수정 시 사이드이펙트 예측 불가 | 코드 수정을 회피, 기술부채 누적 |
| **수동 검증 비용** | 매번 사람이 직접 테스트 | 배포 주기 길어짐, 야근 |
| **회귀 버그** | 기존 기능이 새 코드로 깨짐 | 핫픽스 반복, 신뢰도 하락 |
| **온보딩 장벽** | 신규 인원이 코드 의도 파악 어려움 | 생산성 저하 |

### 1.2. 테스트 자동화가 해결하는 것

테스트 코드는 **코드의 안전망**입니다.

- **변경에 대한 자신감**: 수정 후 테스트를 돌리면 사이드이펙트를 즉시 감지
- **자동 회귀 검증**: 매 빌드마다 기존 기능이 정상인지 자동 확인
- **살아있는 문서**: 테스트 코드 자체가 "이 메서드는 이렇게 동작해야 한다"는 명세
- **리팩토링 지원**: 테스트가 보장하므로 안심하고 코드 개선 가능

### 1.3. AI 에이전트를 활용하는 이유

레거시 프로젝트에 테스트를 추가하는 것은 현실적으로 어렵습니다.

| 기존 방식 (수동) | AI 에이전트 활용 |
|---|---|
| 소스 코드를 읽고 이해하는데 시간 소요 | 소스 코드를 자동 분석 |
| 테스트 패턴을 매번 직접 구성 | 템플릿 기반으로 일관된 테스트 생성 |
| 사람마다 스타일이 다름 | 코드 스타일 규칙을 100% 준수 |
| Mock 설정, 어설션 작성에 시간 소요 | 자동 생성 후 검증까지 수행 |
| 클래스 10개 테스트 작성에 며칠 소요 | 수분~수십분 내 생성 |

---

## 2. What — 무엇을 할 것인가

이 가이드는 **두 단계**로 구성됩니다.

### Phase 1: 기존 소스에 테스트코드 생성 (Test-After)

이미 구현된 코드에 대해 AI 에이전트가 테스트를 자동 생성합니다.

```
기존 소스 코드 → AI 에이전트 분석 → 테스트 코드 생성 → 컴파일 → 실행 → 커버리지 확인
```

### Phase 2: 신규 기능을 TDD로 개발 (Test-First)

테스트 안전망이 구축된 후, 새로운 기능을 추가할 때는 TDD 방식으로 개발합니다.

```
요구사항 → 테스트 먼저 작성(Red) → 최소 구현(Green) → 리팩토링(Refactor)
```

### 최종 목표

```
테스트 없는 레거시 → [Phase 1] 안전망 구축 → [Phase 2] TDD로 신규 개발 → TDD 문화 정착
```

---

## 3. How — 환경 세팅

### 3.1. 프로젝트에 에이전트 파일 배치

Claude Code의 에이전트(Agent)는 `.claude/agents/` 디렉토리에 마크다운 파일로 정의합니다.
에이전트는 AI에게 **"너는 이런 역할이야, 이렇게 행동해"**라고 지시하는 행동 지시서입니다.

프로젝트 루트에 다음 파일들을 배치합니다.

```
{프로젝트 루트}/
├── .claude/
│   └── agents/
│       ├── test-generator.md     ← 테스트 생성 에이전트
│       └── tdd-review.md         ← 테스트 리뷰 에이전트
```

### 3.2. 에이전트 역할 및 동작 방식

이 프로젝트에서는 **2개의 전문 에이전트**가 역할을 분담합니다.

#### test-generator (테스트 생성 에이전트)

| 항목 | 내용 |
|---|---|
| **역할** | Java 소스 코드를 분석하여 JUnit 5 테스트 코드를 자동 생성 |
| **입력** | 클래스명 (예: `UserService`) |
| **출력** | 완성된 테스트 파일 (`UserServiceTest.java`) |

**동작 흐름**:

```
1. 소스 파일 탐색 (Glob 패턴으로 자동 검색)
2. 소스 코드 분석 (public 메서드, 분기, 예외, 의존성 파악)
3. 클래스 유형 판별 (Service / Controller / Mapper / Util)
4. 해당 유형의 템플릿 선택 (docs/ai-tdd-skills/templates/)
5. 4-Level 테스트 생성:
   - Level 1: Happy Case (정상 동작, 각 메서드당 1개)
   - Level 2: Edge Case (경계값, 분기 커버)
   - Level 3: Exception (예외 경로, throw문 1:1)
   - Level 4: Mutation (변이 감지 테스트)
6. 컴파일 검증 → 실행 검증 → 커버리지 확인
```

#### tdd-review (테스트 리뷰 에이전트)

| 항목 | 내용 |
|---|---|
| **역할** | 생성된 테스트 코드의 품질을 검증하고 점수를 매김 |
| **입력** | 테스트 클래스명 (예: `UserServiceTest`) |
| **출력** | 100점 만점 리뷰 리포트 (A~F 등급) |

**검증 영역**:

| 영역 | 배점 | 검증 항목 |
|---|---|---|
| 구조 검증 | 30점 | 4-Level 구조, Given-When-Then, @Nested, 테스트 수 |
| 품질 검증 | 40점 | AssertJ, Mock 정확성, verify, @DisplayName, 독립성 |
| NH 규칙 | 30점 | PII 마스킹, 더미 데이터, 어노테이션 적정 사용 |

**등급 기준**: A(90+) 우수 / B(80~89) 양호 / C(70~79) 보통 / D(60~69) 미흡 / F(60 미만) 불합격

#### 에이전트 간 협업 흐름

```
[test-generator]          [tdd-review]
     │                         │
     ├─ 테스트 생성 ──────────→ │
     │                         ├─ 품질 검증
     │                         ├─ 점수 산출
     │  ←── 개선 권고 ──────── ┤
     │                         │
     ├─ 테스트 수정 ──────────→ │
     │                         ├─ 재검증
     │  ←── 통과 ────────────  ┤
```

### 3.3. 스킬 문서 배치 (docs/ai-tdd-skills/)

스킬 문서는 에이전트가 참조하는 **지식 베이스**입니다.
에이전트가 "어떤 규칙을 따를지, 어떤 패턴으로 생성할지"를 이 문서에서 읽습니다.

프로젝트에 다음 구조로 복사합니다.

```
{프로젝트 루트}/
└── docs/
    └── ai-tdd-skills/
        ├── .claude.md                ← [커스터마이징 필요] 프로젝트 설정
        ├── generation-guide.md       ← 생성 가이드 (4-Level, 판별 기준)
        ├── document-guide.md         ← 문서 체계 설명
        │
        ├── templates/                ← 계층별 테스트 템플릿
        │   ├── service-test.md           Service 단위테스트
        │   ├── controller-test.md        Controller 슬라이스 테스트
        │   ├── mapper-test.md            Mapper Mock/DB 테스트
        │   └── util-test.md              Utility 순수 테스트
        │
        ├── constraints/              ← 규칙 및 제약사항
        │   ├── nh-rules.md               NH 도메인 특화 규칙 (최우선)
        │   ├── naming-conventions.md     네이밍 규칙
        │   ├── code-style.md             코드 스타일
        │   └── test-coverage.md          커버리지 기준
        │
        ├── references/examples/      ← 참고 예제
        │   ├── service-test-example.md
        │   ├── controller-test-example.md
        │   ├── mapper-test-example.md
        │   └── util-test-example.md
        │
        └── verification/             ← 검증 절차
            ├── compile-check.md          컴파일 검증
            ├── test-execution.md         테스트 실행 검증
            └── coverage-report.md        커버리지 검증
```

**에이전트가 문서를 참조하는 순서**:

```
.claude.md (프로젝트 설정)
    → 소스 코드 분석
    → generation-guide.md (생성 가이드)
    → templates/{계층}-test.md (템플릿 선택)
    → constraints/ (규칙 적용)
    → references/examples/ (예제 참조)
    → 테스트 생성
    → verification/ (검증 수행)
```

### 3.4. 프로젝트 설정 파일 작성 (.claude.md)

`docs/ai-tdd-skills/.claude.md` 파일에서 `[수정필요]` 항목을 프로젝트에 맞게 변경합니다.

```markdown
## 프로젝트 정보

| 항목 | 값 | 비고 |
|---|---|---|
| 프로젝트명 | 내 프로젝트명 | `[수정필요]` |
| 프레임워크 버전 | 2.7.17 | `[수정필요]` |
| JDK 버전 | 1.8 | `[수정필요]` |
| 빌드 도구 버전 | 6.8.3 | `[수정필요]` |

## 프로젝트 구조

| 항목 | 경로 | 비고 |
|---|---|---|
| 기본 패키지 | `com.nhcard.al.demo` | `[수정필요]` 실제 패키지로 변경 |
```

### 3.5. 빌드 설정 (JaCoCo, PIT)

테스트 커버리지 측정과 뮤테이션 테스트를 위한 플러그인을 `build.gradle`에 추가합니다.

#### JaCoCo 설정 (커버리지 측정)

```groovy
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.7"
}

jacocoTestReport {
    reports {
        xml.enabled = true
        html.enabled = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = 'LINE'
                minimum = 0.80      // 라인 커버리지 80%
            }
            limit {
                counter = 'BRANCH'
                minimum = 0.70      // 분기 커버리지 70%
            }
        }
    }
}
```

#### PIT 설정 (뮤테이션 테스트, 선택사항)

```groovy
plugins {
    id 'info.solidsoft.pitest' version '1.7.4'
}

pitest {
    targetClasses = ['com.nhcard.al.demo.*']    // 프로젝트 패키지에 맞게 변경
    targetTests = ['com.nhcard.al.demo.*']
    mutationThreshold = 65
    outputFormats = ['HTML']
}
```

### 3.6. 에이전트 동작 확인

모든 파일을 배치한 후, Claude Code에서 에이전트가 정상 인식되는지 확인합니다.

```bash
# 1. 프로젝트 디렉토리에서 Claude Code 실행
claude

# 2. 에이전트 목록 확인 (목록 확인용, 에이전트 호출과는 무관)
/agents
```

다음과 같이 표시되면 정상입니다.

```
❯ Create new agent

  Project agents (~\{프로젝트}\  .claude\agents)
  test-generator · inherit
  tdd-review · inherit
```

> **참고**: `/agents` 명령어는 에이전트 **목록 확인 및 생성/수정**용입니다. 에이전트를 호출하는 방법은 다음 절(3.7)을 참조하세요.

### 3.7. 에이전트 사용법

#### 핵심: 에이전트는 설치만 하면 바로 사용 가능

`.claude/agents/` 디렉토리에 에이전트 파일이 배치되어 있으면, Claude Code가 자동으로 인식합니다.
별도의 선택 과정 없이 **에이전트 이름과 함께 입력하면 바로 실행**됩니다.

> 참고: `/agents` 명령어는 에이전트 파일을 **생성/수정**할 때 사용하는 것이지, 에이전트를 호출하는 방법이 아닙니다.

#### 한 줄로 테스트 생성

Claude Code 프롬프트에 다음과 같이 입력합니다:

```
test-generator, UserService.java
```

이것만으로 에이전트가 다음을 **자동으로** 수행합니다:

```
[자동] src/main/java/**/UserService.java 파일 탐색
[자동] 소스 코드 분석 (메서드, 분기, 예외, 의존성)
[자동] Service 계층 판별 → service-test.md 템플릿 적용
[자동] 4-Level 테스트 생성
[자동] 컴파일 확인 → 오류 시 자동 수정
[자동] 테스트 실행 → 실패 시 자동 수정
[자동] 커버리지 확인
```

#### 다양한 입력 방식

| 입력 | 설명 |
|---|---|
| `test-generator, UserService.java` | 클래스명만으로 테스트 생성 (가장 간단) |
| `test-generator, UserService 테스트 코드 생성` | 한국어 지시와 함께 |
| `test-generator, service 패키지 전체 테스트 코드 생성` | 패키지 단위 배치 생성 |
| `test-generator, UserService 커버리지 미달 영역 추가 테스트` | 보충 테스트 생성 |

#### 터미널 전체 흐름 예시

> 아래는 에이전트 실행 시 **예상되는 출력 흐름**입니다. 실제 출력은 프로젝트와 모델에 따라 다를 수 있습니다.

```bash
$ claude

> test-generator, UserService.java

# === 에이전트 자동 실행 ===

소스 파일 탐색 중...
  → src/main/java/com/nhcard/al/demo/service/UserService.java 발견

소스 코드 분석 중...
  → public 메서드: 6개
  → 의존성: UserMapper, PasswordEncoder
  → 분기(if/switch): 4개
  → throw 문: 3개
  → 계층: Service (@Service)

테스트 수 산출:
  → L1(Happy): 6개, L2(Edge): 4개, L3(Exception): 3개, L4(Mutation): 6개
  → 총 19개 테스트

테스트 코드 생성 중...
  → src/test/java/.../service/UserServiceTest.java 생성 완료

컴파일 검증 중...
  → ./gradlew compileTestJava
  → BUILD SUCCESSFUL

테스트 실행 중...
  → ./gradlew test --tests "*.UserServiceTest"
  → 19 tests completed, 19 passed
  → BUILD SUCCESSFUL

커버리지 확인 중...
  → ./gradlew jacocoTestReport
  → UserService: 라인 87%, 분기 75% ✅ 기준 충족

✅ UserServiceTest.java 생성 완료 (19개 테스트, 커버리지 충족)
```

#### tdd-review 에이전트 사용법

테스트 생성 후 품질 리뷰가 필요하면, 같은 방식으로 리뷰 에이전트를 호출합니다:

```bash
> tdd-review, UserServiceTest 리뷰

# === 리뷰 에이전트 자동 실행 ===

## TDD 리뷰 결과: UserServiceTest
### 종합 점수: 92/100 (A등급)
...
```

별도의 전환 과정 없이, 에이전트 이름만 바꿔서 입력하면 됩니다.

#### 요약: 일상적인 사용 패턴

```
[매일 반복하는 흐름]

1. claude 실행
2. test-generator, {클래스명}.java 입력
3. 에이전트가 테스트 생성 + 검증 자동 수행
4. 필요시 tdd-review, {테스트클래스명} 리뷰 입력하여 품질 확인
5. 수정 필요하면 test-generator에게 보충 요청
```

---

## 4. How — Phase 1: 기존 소스 테스트 생성

### 4.1. 대상 클래스 선정

모든 클래스를 한번에 하지 않습니다. 다음 우선순위로 점진적으로 진행합니다.

| 순서 | 대상 | 이유 |
|---|---|---|
| 1 | **Utility 클래스** | 의존성 없어 가장 쉬움, 빠른 성공 경험 |
| 2 | **Service 클래스** | 핵심 비즈니스 로직, 투자 대비 효과 최대 |
| 3 | **Controller 클래스** | HTTP 레이어 검증 |
| 4 | **Mapper 클래스** | DB 연동 테스트 (Mock 기반 우선) |

### 4.2. 단일 클래스 테스트 생성 실습

**예시**: `UserService` 클래스의 테스트를 생성합니다.

#### Step 1: 에이전트 호출

```bash
> test-generator, UserService.java
```

에이전트가 다음을 자동으로 수행합니다:

```
[자동] 소스 파일 탐색:
  → src/main/java/**/UserService.java 검색
  → 파일 발견: src/main/java/com/nhcard/al/demo/service/UserService.java

[자동] 소스 코드 분석:
  → public 메서드 6개 발견
  → 의존성: UserMapper, PasswordEncoder
  → 분기(if/switch): 4개
  → throw 문: 3개
  → 계층 판별: Service (@Service 어노테이션)

[자동] 테스트 수 산출:
  → L1 (Happy Case): 6개 (public 메서드 수)
  → L2 (Edge Case): 4개 (분기 수)
  → L3 (Exception): 3개 (throw 문 수)
  → L4 (Mutation): 6개 (public 메서드 수)
  → 총 19개 테스트 생성 예정

[자동] 템플릿 선택: service-test.md
[자동] 테스트 코드 생성 중...
```

#### Step 2: 생성된 테스트 파일 확인

에이전트가 다음 경로에 파일을 생성합니다:

```
src/test/java/com/nhcard/al/demo/service/UserServiceTest.java
```

생성되는 테스트 코드 구조:

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
    @Nested
    @DisplayName("Level 1: Happy Case")
    class HappyCases {

        @Test
        @DisplayName("유효한 요청으로 사용자 등록 성공")
        void should_registerUser_when_validRequest() {
            // Given
            ...
            // When
            ...
            // Then
            ...
        }
        // ... 각 public 메서드당 1개
    }

    // ── Level 2: Edge Case ──
    // ... 분기 조건별 테스트

    // ── Level 3: Exception ──
    // ... throw 문 1:1 매핑

    // ── Level 4: Mutation Testing ──
    // ... 변이 감지 테스트
}
```

#### Step 3: 컴파일 확인

```bash
./gradlew compileTestJava
```

**정상 출력:**

```
> Task :compileTestJava

BUILD SUCCESSFUL in 3s
2 actionable tasks: 1 executed, 1 up-to-date
```

**만약 컴파일 오류가 발생하면**, 에이전트가 자동으로 오류를 분석하고 수정합니다:

```
[자동] 컴파일 오류 감지:
  → error: cannot find symbol - UserRegistrationRequest
  → 해결: import 문 추가
  → 재컴파일 시도...
  → BUILD SUCCESSFUL
```

#### Step 4: 테스트 실행

```bash
./gradlew test --tests "com.nhcard.al.demo.service.UserServiceTest"
```

**정상 출력:**

```
> Task :test

UserServiceTest > Level 1: Happy Case > 유효한 요청으로 사용자 등록 성공 PASSED
UserServiceTest > Level 1: Happy Case > ID로 사용자 조회 성공 PASSED
UserServiceTest > Level 2: Edge Case > 이메일이 null이면 예외 발생 PASSED
...
UserServiceTest > Level 4: Mutation Testing > 사용자 등록 시 모든 필드 정확히 설정됨 PASSED

19 tests completed, 19 passed

BUILD SUCCESSFUL in 5s
```

#### Step 5: 커버리지 확인

```bash
./gradlew test jacocoTestReport
```

**보고서 확인:**

```
build/reports/jacoco/test/html/index.html
```

브라우저에서 열면 클래스별 커버리지를 확인할 수 있습니다:

```
Package                          Line Coverage    Branch Coverage
com.nhcard.al.demo.service       87%              75%
  UserService                    87%              75%
```

- 초록색: 테스트로 커버된 코드
- 빨간색: 테스트가 없는 코드
- 노란색: 분기 중 일부만 커버

### 4.3. 배치 생성 (패키지 단위)

여러 클래스를 한번에 생성할 수 있습니다.

```bash
> test-generator, service 패키지 전체 테스트 코드 생성
```

에이전트가 자동으로 의존성 순서를 판별하여 생성합니다:

```
[자동] service 패키지 스캔:
  → UserService.java
  → AuthService.java
  → NoticeService.java

[자동] 의존성 기반 생성 순서:
  1. UserService (기본 도메인)
  2. AuthService (UserService 의존)
  3. NoticeService (독립)

[자동] 생성 진행:
  [1/3] UserServiceTest.java     ✅ 생성 + 컴파일 + 테스트 통과
  [2/3] AuthServiceTest.java     ✅ 생성 + 컴파일 + 테스트 통과
  [3/3] NoticeServiceTest.java   ✅ 생성 + 컴파일 + 테스트 통과

결과: 3개 클래스, 총 52개 테스트 생성 완료
```

### 4.4. 리뷰 에이전트로 품질 확인

생성된 테스트의 품질을 검증합니다.

```bash
> tdd-review, UserServiceTest 리뷰
```

**리뷰 결과 예시:**

```
## TDD 리뷰 결과: UserServiceTest

### 종합 점수: 92/100 (A등급)

| 영역 | 점수 | 만점 |
|---|---|---|
| 구조 검증 | 28/30 | 30 |
| 품질 검증 | 37/40 | 40 |
| NH 규칙 | 27/30 | 30 |

### 감점 상세
| # | 영역 | 항목 | 감점 | 설명 |
|---|---|---|---|---|
| 1 | 구조 | @Nested 미사용 | -2 | L2에 @Nested 래퍼 없음 |
| 2 | 품질 | verify 누락 | -3 | deleteUser의 mapper.delete() verify 없음 |
| 3 | NH규칙 | 테스트 데이터 | -3 | 변수명에 test 접두어 미사용 |

### 안티패턴 탐지
안티패턴 없음

### 개선 권고사항
1. L2 Edge Case에 @Nested 래퍼 추가
2. deleteUser 테스트에 verify(userMapper).delete(userId) 추가
3. 테스트 데이터 변수명을 testName, testEmail 형식으로 변경
```

### 4.5. 커버리지 목표 달성 확인

전체 테스트 생성 후, 프로젝트 전체 커버리지를 확인합니다.

```bash
# 전체 테스트 + 커버리지 보고서 + 기준 검증
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

**합격 시:**

```
> Task :test
> Task :jacocoTestReport
> Task :jacocoTestCoverageVerification

BUILD SUCCESSFUL in 12s
```

**미달 시:**

```
> Task :jacocoTestCoverageVerification FAILED

FAILURE: Build failed with an exception.
* What went wrong:
Rule violated for bundle demo: lines covered ratio is 0.72, but expected minimum is 0.80
```

미달이면 에이전트에게 추가 테스트를 요청합니다:

```
> test-generator, UserService 커버리지 미달 영역 추가 테스트 생성
```

---

## 5. How — Phase 2: 신규 기능 TDD 개발

Phase 1에서 기존 코드에 테스트 안전망을 구축했다면, 이제 새로운 기능을 추가할 때는 **TDD(Test-Driven Development)** 방식으로 개발합니다.

### 5.1. Red-Green-Refactor 사이클 (실패-통과-개선)

TDD는 **3단계 사이클**을 반복하는 개발 방법론입니다.

```
  ┌────────────────────────────────────────────┐
  │                                            │
  │   Red ──→ Green ──→ Refactor ──→ Red ...   │
  │                                            │
  └────────────────────────────────────────────┘
```

| 단계 | 행동 | 상태 |
|---|---|---|
| **Red** | 테스트를 먼저 작성 → 실행하면 실패 (구현이 없으니까) | 빨간불 |
| **Green** | 테스트를 통과하는 **최소한의** 코드 구현 | 초록불 |
| **Refactor** | 테스트가 통과하는 상태를 유지하면서 코드 개선 | 초록불 유지 |

### 5.2. 시나리오: 기존 프로젝트에 "즐겨찾기" 기능 TDD로 추가

> **참고**: 아래는 TDD 프로세스를 설명하기 위한 **가상의 시나리오**입니다. 실제 데모 프로젝트에는 FavoriteService가 포함되어 있지 않습니다.

기존 데모 프로젝트에 **사용자가 공지사항을 즐겨찾기하는 기능**을 TDD로 개발합니다.

#### 요구사항

```
- 사용자가 공지사항을 즐겨찾기에 추가할 수 있다
- 사용자가 자신의 즐겨찾기 목록을 조회할 수 있다
- 사용자가 즐겨찾기를 삭제할 수 있다
- 이미 즐겨찾기한 공지사항을 다시 추가하면 예외 발생
```

#### Step 1: Red — 테스트 먼저 작성

에이전트에게 요구사항을 주고 테스트를 먼저 생성합니다.

```
> test-generator, FavoriteService 테스트 코드 생성

요구사항:
- 사용자가 공지사항을 즐겨찾기에 추가할 수 있다
- 사용자가 자신의 즐겨찾기 목록을 조회할 수 있다
- 사용자가 즐겨찾기를 삭제할 수 있다
- 이미 즐겨찾기한 공지사항을 다시 추가하면 예외 발생
```

에이전트가 테스트 코드를 생성합니다:

```java
// src/test/java/.../service/FavoriteServiceTest.java

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteMapper favoriteMapper;

    @InjectMocks
    private FavoriteService favoriteService;

    // ── Level 1: Happy Case ──
    @Nested
    @DisplayName("Level 1: Happy Case")
    class HappyCases {

        @Test
        @DisplayName("공지사항 즐겨찾기 추가 성공")
        void should_addFavorite_when_validRequest() {
            // Given
            Long testUserId = 1L;
            Long testNoticeId = 100L;
            when(favoriteMapper.findByUserIdAndNoticeId(testUserId, testNoticeId))
                    .thenReturn(null);

            // When
            favoriteService.addFavorite(testUserId, testNoticeId);

            // Then
            verify(favoriteMapper).insert(any(Favorite.class));
        }
    }

    // ── Level 3: Exception ──
    @Test
    @DisplayName("이미 즐겨찾기한 공지사항이면 예외 발생")
    void should_throwException_when_alreadyFavorited() {
        // Given
        Long testUserId = 1L;
        Long testNoticeId = 100L;
        when(favoriteMapper.findByUserIdAndNoticeId(testUserId, testNoticeId))
                .thenReturn(new Favorite());

        // When & Then
        assertThatThrownBy(() -> favoriteService.addFavorite(testUserId, testNoticeId))
                .isInstanceOf(BusinessException.class)
                .hasMessage("이미 즐겨찾기에 추가된 공지사항입니다.");
    }
}
```

이 시점에서 컴파일하면 **실패합니다** (아직 구현이 없으니까):

```bash
./gradlew compileTestJava
```

```
> Task :compileTestJava FAILED

error: cannot find symbol
  symbol: class FavoriteService
error: cannot find symbol
  symbol: class FavoriteMapper
error: cannot find symbol
  symbol: class Favorite

BUILD FAILED in 2s
```

이것이 **Red** 상태입니다. 테스트가 실패하는 것이 정상이고, 이제 구현을 시작합니다.

#### Step 2: Green — 최소 구현

테스트를 통과시키기 위한 **최소한의 코드**를 작성합니다.

```java
// 1. 엔티티 생성
public class Favorite {
    private Long id;
    private Long userId;
    private Long noticeId;
    // getter, setter
}

// 2. Mapper 인터페이스 생성
@Mapper
public interface FavoriteMapper {
    Favorite findByUserIdAndNoticeId(@Param("userId") Long userId,
                                      @Param("noticeId") Long noticeId);
    int insert(Favorite favorite);
    List<Favorite> findByUserId(Long userId);
    int deleteByUserIdAndNoticeId(@Param("userId") Long userId,
                                   @Param("noticeId") Long noticeId);
}

// 3. Service 구현
@Service
public class FavoriteService {

    private final FavoriteMapper favoriteMapper;

    public FavoriteService(FavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    public void addFavorite(Long userId, Long noticeId) {
        Favorite existing = favoriteMapper.findByUserIdAndNoticeId(userId, noticeId);
        if (existing != null) {
            throw new BusinessException("이미 즐겨찾기에 추가된 공지사항입니다.");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setNoticeId(noticeId);
        favoriteMapper.insert(favorite);
    }
}
```

다시 테스트를 실행합니다:

```bash
./gradlew test --tests "com.nhcard.al.demo.service.FavoriteServiceTest"
```

```
> Task :test

FavoriteServiceTest > 공지사항 즐겨찾기 추가 성공 PASSED
FavoriteServiceTest > 이미 즐겨찾기한 공지사항이면 예외 발생 PASSED

2 tests completed, 2 passed

BUILD SUCCESSFUL in 4s
```

**Green** 상태가 되었습니다.

#### Step 3: Refactor — 코드 개선

테스트가 통과하는 상태를 유지하면서 코드를 개선합니다.
예를 들어: 유효성 검증 추가, 메서드명 개선, 중복 제거 등.

리팩토링 후 반드시 테스트를 재실행하여 Green 상태를 유지하는지 확인합니다:

```bash
./gradlew test --tests "com.nhcard.al.demo.service.FavoriteServiceTest"
```

```
BUILD SUCCESSFUL
```

이 사이클을 반복하여 나머지 기능(목록 조회, 삭제)도 완성합니다.

### 5.3. TDD 전체 흐름 요약

```
요구사항 정의
    ↓
[Red] 에이전트로 테스트 생성 → 컴파일 실패 확인
    ↓
[Green] 최소 구현 코드 작성 → 테스트 통과 확인
    ↓
[Refactor] 코드 개선 → 테스트 재실행 → Green 유지
    ↓
다음 기능으로 반복
    ↓
전체 커버리지 확인 + 리뷰 에이전트
```

---

## 6. 트러블슈팅 & FAQ

### Q1. 에이전트가 소스 파일을 못 찾아요

```
원인: 패키지 구조가 표준과 다르거나, 소스 경로가 다름
해결: docs/ai-tdd-skills/.claude.md의 "기본 패키지" 항목을 확인하고 수정
```

### Q2. 생성된 테스트가 컴파일 실패해요

```
원인: import 누락, 의존성 타입 불일치 등
해결: 에이전트가 자동으로 최대 3회 재시도함. 3회 후에도 실패하면
     에러 메시지를 에이전트에게 알려주면 수정함
```

### Q3. 커버리지가 목표에 미달해요

```
원인: 예외 경로, else 분기 등이 미커버
해결: 에이전트에게 "커버리지 미달 영역 추가 테스트 생성" 요청
     coverage-report.md의 미달 시 조치 흐름 참조
```

### Q4. Mock 설정이 실제 코드와 안 맞아요

```
원인: 소스 코드 변경 후 테스트 미갱신
해결: 에이전트에게 해당 클래스 테스트 재생성 요청
```

### Q5. 에이전트가 동작하지 않아요

```
원인: 파일 위치나 형식 오류
해결:
  1. .claude/agents/ 디렉토리가 프로젝트 루트에 존재하는지 확인
  2. 파일 확장자가 .md인지 확인
  3. 파일 상단에 YAML frontmatter(--- 블록)가 있는지 확인
  4. /agents 명령어로 에이전트 목록에 표시되는지 확인
```

### Q6. 폐쇄망에서 Gradle 의존성을 못 받아요

```
원인: 인터넷 차단 환경
해결: 사내 Nexus/Artifactory에 필요한 의존성 사전 등록
     필요한 의존성 목록: docs/closed-network-dependencies.md 참조
```

---

## 7. Next — TDD 문화 확산

### 7.1. 팀 도입 단계별 로드맵

| 단계 | 기간 | 목표 | 행동 |
|---|---|---|---|
| **1단계: 파일럿** | 1~2주 | 핵심 인원 1~2명이 도구 익히기 | 이 가이드 따라 실습 |
| **2단계: 확산** | 2~4주 | 팀 전체 적용 | 기존 코드 Phase 1 진행, 커버리지 목표 설정 |
| **3단계: 습관화** | 1~2개월 | 신규 기능에 TDD 적용 | Phase 2 적용, 코드 리뷰에 테스트 포함 |
| **4단계: 문화** | 3개월~ | TDD가 기본 개발 방식 | "테스트 없는 코드는 머지 불가" 정책 |

### 7.2. 코드 리뷰에 테스트 포함 기준

Pull Request 시 다음 항목을 체크합니다:

```
[ ] 신규/수정된 코드에 대응하는 테스트가 있는가?
[ ] 테스트가 모두 통과하는가? (./gradlew test)
[ ] 라인 커버리지 80% 이상인가?
[ ] tdd-review 에이전트 점수가 80점(B등급) 이상인가?
```

### 7.3. 점진적 도입 전략

```
"완벽한 커버리지"를 목표로 하지 마세요.

먼저 가장 중요한 Service 클래스부터 시작하고,
변경이 잦은 코드부터 테스트를 추가하세요.
새로운 코드는 반드시 테스트와 함께 작성하는 습관을 들이세요.

100%가 아닌 "어제보다 나은 커버리지"가 목표입니다.
```
