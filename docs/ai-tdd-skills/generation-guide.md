# AI TDD 스킬 가이드 (공통 생성 가이드)

> 이 문서는 AI TDD 에이전트가 테스트 코드를 생성할 때 참조하는 **핵심 가이드**입니다.
> 클래스 유형 판별, 4단계 테스트 레벨, 규칙 적용, 검증 기준을 정의합니다.

---

## 1. 테스트 생성 프로세스

에이전트가 테스트 생성 요청을 받으면 다음 순서로 처리합니다.

```
1. 프로젝트 설정 확인 (.claude.md)
2. 대상 소스 코드 분석
3. 클래스 유형 판별 (→ 템플릿 선택)
4. 규칙 적용 (constraints/)
5. 예제 참조 (references/)
6. 4단계 레벨별 테스트 코드 생성
7. 검증 (verification/)
```

### 1.1. 클래스 유형 판별 기준

대상 클래스의 **어노테이션과 패턴**으로 유형을 판별하고, 해당 템플릿을 선택합니다.

| 어노테이션/패턴 | 유형 | 템플릿 |
|---|---|---|
| `@Service`, `@Component` (비즈니스 로직) | Service | `templates/service-test.md` |
| `@RestController`, `@Controller` | Controller | `templates/controller-test.md` |
| `@Mapper`, Mybatis 인터페이스 | Mapper | `templates/mapper-test.md` |
| static 메서드 위주, Helper/Util 클래스 | Utility | `templates/util-test.md` |

**판별 우선순위**: 어노테이션 > 클래스명 패턴 > 패키지 경로
- 어노테이션이 없으면 클래스명(`*Service`, `*Controller` 등)으로 판별
- 그래도 불분명하면 패키지 경로(`service/`, `controller/` 등)로 판별

**판별 불가시 처리**:
위 기준으로 유형 판별이 불가능한 경우 `Utility` 유형을 기본값으로 적용하고 테스트 생성 결과 보고서에 "클래스 유형을 특정할 수 없어 Utility 기본 템플릿을 사용함"이라는 경고 메시지를 포함합니다.

### 1.2. 규칙 적용 순서

테스트 코드 생성 시 다음 순서로 규칙을 적용합니다.

| 순서 | 파일 | 적용 내용 |
|---|---|---|
| 1 | `constraints/nh-rules.md` | NH 도메인 규칙 (데이터 보호, 보안, 감사로그) |
| 2 | `constraints/naming-conventions.md` | 클래스/메서드/변수 네이밍 표준 |
| 3 | `constraints/code-style.md` | 코딩 스타일 (포맷, 구조, 패턴) |
| 4 | `constraints/test-coverage.md` | 커버리지 기준 (라인 80%, 분기 70%) |

> 규칙 간 충돌 시: **nh-rules.md의 보안/도메인 규칙이 최우선**

---

## 2. 4단계 테스트 레벨

모든 테스트는 다음 **4단계 레벨 우선순위**로 생성합니다.

> **L1 (Happy Case)** → **L2 (Edge Case)** → **L3 (Exception)** → **L4 (Mutation)**
> - L1을 먼저 충분히 보장하고, L2, L3, L4 순으로 Coverage 확장
> - 비율보다 **비즈니스 로직 검증 충분성**이 우선

### Level 1: Happy Case - 정상 케이스

**목적**: 정상 입력으로 기대 결과가 나오는지 검증

| 항목 | 설명 |
|---|---|
| 대상 | 각 public 메서드의 정상 시나리오 |
| 패턴 | Given(정상 입력) → When(메서드 호출) → Then(기대 결과 검증) |

**생성 기준**:
- 모든 public 메서드에 대해 **1개** Happy Case 필수
- 반환값 검증 (필드값 일치 여부)
- `@DisplayName`에 한글로 시나리오 설명

> **설계 원칙**: L1은 "정상 동작 검증"에 집중하고, "호출 검증"은 L4(Mutation)에서 수행

**예시 패턴**:
```
"유효한 요청으로 사용자 생성 성공"
"정상 ID로 사용자 조회 성공"
"유효한 로그인 정보로 인증 성공"
```

### Level 2: Edge Case - 경계값/특수 입력

**목적**: 경계값, Null/Empty, 특수 입력에 대한 처리 검증

| 항목 | 설명 |
|---|---|
| 대상 | 파라미터 경계값, null/empty, 타입 한계값 |
| 패턴 | Given(경계 입력) → When(메서드 호출) → Then(예외 또는 적절한 처리 검증) |

**생성 기준**:
- 각 파라미터에 대해 null, empty(""), 공백(" ") 테스트
- 숫자 파라미터: 0, 음수, Long.MAX_VALUE 등 경계값
- 문자열 파라미터: 특수문자, 최대 길이, 한글/영문
- 컬렉션 파라미터: 빈 리스트, null 리스트

**예시 패턴**:
```
"ID가 null이면 InvalidUserIdException"
"ID가 0이면 InvalidUserIdException"
"이메일이 빈 문자열이면 예외 발생"
```

### L2 vs L3 구분 기준

| 레벨 | 구분 | 검증 대상 |
|---|---|---|
| **L2 (Edge Cases)** | 파라미터 자체의 유효성 검증 | `null`, `empty`, `0`, 음수, 경계값 |
| **L3 (Exception Cases)** | 비즈니스 로직에 의한 예외 | DB 조회 결과에 따른 예외 (중복, 미존재, 권한 없음) |

**주의**: L2와 L3의 테스트가 중복되지 않도록 주의합니다.
- L2: 파라미터 값 자체가 유효하지 않은 경우
- L3: 파라미터는 유효하지만 비즈니스 로직상 예외가 발생하는 경우

### Level 3: Exception - 비즈니스/시스템 예외 (throw 문 1:1)

**목적**: 비즈니스 규칙 위반, DB 오류, 외부 시스템 오류 처리 검증

| 항목 | 설명 |
|---|---|
| 대상 | throw 문, try-catch, 비즈니스 검증 로직 |
| 패턴 | Given(예외 조건) → When(메서드 호출) → Then(예외 타입 + 메시지 검증) |

**생성 기준**:
- 소스 코드의 모든 throw 문에 대해 테스트 생성
- 예외 타입(isInstanceOf)과 메시지(hasMessage) 모두 검증
- 외부 의존성 실패 시나리오 (DB 오류, 타임아웃 등)
- assertThatThrownBy() 사용

**예시 패턴**:
```
"중복 이메일이면 DuplicateEmailException 발생"
"사용자 미존재시 UserNotFoundException 발생"
"DB 연결 실패시 적절한 예외 처리"
```

### Level 4: Mutation Testing - 숨겨진 버그/조건분기 (각 메서드 1개 - 필수)

**목적**: 조건문 변이, 부수효과 누락, 숨겨진 버그 검증

| 항목 | 설명 |
|---|---|
| 대상 | 조건분기(if/else), 메서드 호출 순서, 부수효과 |
| 패턴 | Given(특정 조건) → When(메서드 호출) → Then(호출 순서/횟수/인자 정밀 검증) |

**생성 기준**:
- verify()로 메서드 호출 여부/횟수/인자값 정밀 검증
- 조건분기의 양쪽 경로 모두 테스트
- 메서드 호출 순서가 중요한 경우 InOrder 검증
- 도메인 특화: 암호화 호출 여부, 감사로그 기록 여부

**예시 패턴**:
```
"사용자 생성시 비밀번호 인코딩이 반드시 호출됨"
"사용자 생성시 insert가 정확히 1번 호출됨"
"이메일 중복 확인이 insert보다 먼저 호출됨"
```

---

## 3. 메서드 시그니처 → 테스트 코드 변환 규칙

에이전트가 소스 분석 결과에서 테스트 코드를 **기계적으로** 생성하기 위한 상세 매핑 테이블입니다.

### 3.1. 파라미터 타입별 Edge Case 상세 매트릭스

| 파라미터 타입 | 테스트 값 | 예상 동작 | 테스트 수 |
|---|---|---|---|
| `String` | `null` | 예외 또는 기본값 반환 | 2 |
| | `""` (빈 문자열) | 예외 또는 기본값 반환 | |
| `Long` | `null` | 예외 발생 | 2~3 |
| | `0L` | 예외 또는 빈 결과 | |
| | `-1L` | 예외 발생 | |
| `Integer` | `null` | 예외 발생 | 2~3 |
| | `0` | 경계값 동작 확인 | |
| | `-1` | 예외 발생 | |
| Object (DTO/Request) | `null` | `NullPointerException` 또는 비즈니스 예외 | 2 |
| | 필수필드 누락 객체 | 유효성 검증 예외 | |
| `List<T>` | `null` | 예외 또는 빈 처리 | 2 |
| | `Collections.emptyList()` | 빈 결과 반환 | |
| `LocalDate` | `null` | 예외 발생 | 1 |
| `LocalDateTime` | `null` | 예외 발생 | 1 |
| `boolean` / `Boolean` | `true`, `false` | 각 분기 경로 실행 | 2 (L4에 포함) |
| `enum` | 각 enum 값 | 값별 분기 실행 | enum 수 (L1/L2에 분배) |

### 3.2. 반환 타입별 어설션 패턴 상세

| 반환타입 | Level 1 (Happy) 어설션 | Level 2 (Edge) 어설션 | 비고 |
|---|---|---|---|
| Object (DTO) | `assertThat(result).isNotNull()` | 파라미터 null → 예외 검증 | 핵심 필드별 `isEqualTo()` 추가 |
| | `assertThat(result.getField()).isEqualTo(expected)` | | |
| `List<T>` | `assertThat(result).hasSize(N)` | `assertThat(result).isEmpty()` | 첫 요소 필드도 검증 |
| | `assertThat(result.get(0).getField()).isEqualTo(expected)` | | |
| `void` | `assertThatCode(() -> sut.method(arg)).doesNotThrowAnyException()` | 파라미터 null → 예외 검증 | `verify()`로 부수효과 필수 검증 |
| | `verify(mock).method(arg)` | | |
| `int` (영향 행 수) | `assertThat(result).isEqualTo(1)` | `assertThat(result).isZero()` | 0은 대상 없음 의미 |
| `boolean` | `assertThat(result).isTrue()` | `assertThat(result).isFalse()` | 양쪽 모두 테스트 |
| `Optional<T>` | `assertThat(result).isPresent()` | `assertThat(result).isEmpty()` | `get()` 후 필드 검증 |

### 3.3. 메서드 시그니처 → @DisplayName 생성 규칙

| 메서드 패턴 | @DisplayName 생성 규칙 | 예시 |
|---|---|---|
| `create{Entity}(Request)`, `register{Entity}(Request)` 등 | "유효한 요청으로 {엔티티} 생성 성공" | `"유효한 요청으로 사용자 생성 성공"` |
| `get{Entity}ById(String)`, `get{Entity}Detail(String)` | "정상 ID로 {엔티티} 조회 성공" | `"정상 ID로 사용자 조회 성공"` |
| `getAll{Entity}s()`, `get{Entity}s` | "전체 {엔티티} 조회 성공" | `"전체 사용자 목록 조회 성공"` |
| `update{Entity}(String, Request)` | "유효한 요청으로 {엔티티} 수정 성공" | `"유효한 요청으로 사용자 수정 성공"` |
| `delete{Entity}(String)` | "{엔티티} 삭제 성공" | `"사용자 삭제 성공"` |
| 예외 테스트 | "{조건}이면 {예외클래스명} 발생" | `"중복 아이디이면 BusinessException 발생"` |
| Edge Case | "{파라미터}가 {값}이면 {예상동작}" | `"ID가 null이면 BusinessException 발생"` |
| Mutation | "{대상} 시 {검증대상} 이 반드시 호출됨" | `"사용자 생성 시 비밀번호 인코딩 반드시 호출됨"` |

---

## 4. 생성 결과물 표준 구조

생성되는 테스트 클래스는 다음 구조를 따릅니다.

```java
class {ClassName}Test {

    // Mock 선언 (소스 클래스의 생성자 파라미터에 대응)
    @Mock
    private {DependencyType} {dependencyName};

    // 테스트 대상
    @InjectMocks
    private {TargetClass} {targetInstance};

    /* -------------------------------------------------
     * Level 1: Happy Cases (각 메서드 1개)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("정상 케이스 테스트")
    class HappyCases {
        @Test
        @DisplayName("한글 시나리오 설명")
        void should_{동작}_when_{조건}() {
            // Given - Mock 설정
            // When - 메서드 호출
            // Then - assertThat 검증
        }
    }

    /* -------------------------------------------------
     * Level 2: Edge Cases
     * ------------------------------------------------- */

    @Nested
    @DisplayName("경계값 테스트")
    class EdgeCases {...}

    /* -------------------------------------------------
     * Level 3: Exception Cases (throw 문 1:1)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("비즈니스 예외 테스트")
    class ExceptionCases {...}

    /* -------------------------------------------------
     * Level 4: Mutation Testing (각 메서드 1개 - 필수)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {...}
}
```

### 4.1. 테스트 유형별 어노테이션

| 유형 | 어노테이션 | 용도 |
|---|---|---|
| Service 단위테스트 | `@ExtendWith(MockitoExtension.class)` | 의존성 Mock, 순수 단위테스트 |
| Controller 슬라이스 테스트 | `@WebMvcTest({Controller}.class)` | MockMvc 기반 HTTP 테스트 |
| Mapper 테스트 | `@MybatisTest` 또는 `@ExtendWith(MockitoExtension.class)` | DB 연동 또는 Mock |
| Utility 테스트 | 어노테이션 불필요 | static 메서드 직접 호출 |

### 4.2. 필수 검증 항목

모든 생성된 테스트에서 다음을 확인합니다.

| 항목 | 검증 방법 |
|---|---|
| 반환값 | `assertThat(result).isNotNull()`, `.isEqualTo()` 등 |
| 예외 | `assertThatThrownBy().isInstanceOf().hasMessage()` |
| 부수효과 | `verify({mock}).{메서드}()` |
| 호출 안됨 | `verify({mock}, never()).{메서드}()` |
| 호출 횟수 | `verify({mock}, times(N)).{메서드}()` |

---

## 5. NH 도메인 특화 검증

NH 프로젝트에서 반드시 확인해야 하는 도메인 규칙입니다.

| 규칙 | 테스트 검증 방법 |
|---|---|
| 개인정보 마스킹 | 주민번호, 카드번호 등 마스킹 처리 결과 검증 |
| Petra 암호화 | 비밀번호 등 암호화 메서드 호출 여부 verify |
| 감사로그 기록 | 핵심 비즈니스 작업 시 감사로그 기록 verify |
| 하드코딩 금지 | 테스트 데이터에 실제 개인정보/자격증명 사용 금지 |

---

## 6. 검증 프로세스

ru생성된 테스트 코드는 `test-generator.md`에 정의된 검증 및 수정 절차에 따라 자동화 스크립트로 검증됩니다.

1.  **자동 검증 스크립트 실행**:
    *   AI 에이전트는 테스트 코드 생성 후, `verification/run-compile-test.sh` 스크립트를 자동으로 실행하여 **컴파일 및 테스트 실행**을 한 번에 검증합니다.
    *   이 스크립트가 성공적으로 완료되면 기본적인 검증을 통과한 것으로 간주합니다.

2.  **수동 심층 분석 (스크립트 실패 시)**:
    *   만약 자동화 스크립트가 실패하면, AI 에이전트는 `verification/compile-check.md`와 `verification/test-execution.md`를 **문제 해결 가이드**로 참조하여 실패 원인을 분석하고 코드를 수정합니다.

3.  **커버리지 검증 (추후 활성화)**:
    *   현재 JaCoCo 라이브러리 반입 대기 중으로, 커버리지 검증은 일시적으로 보류됩니다.
    *   JaCoCo 도입 후에는 `run-compile-test.sh` 스크립트에 커버리지 검증 단계가 포함될 예정입니다.

> 상세한 자동화 흐름 및 자가 수정 정책은 `test-generator.md`의 `[7단계] 검증 및 수정` 섹션을 참조하십시오.