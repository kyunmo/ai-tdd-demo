# AI TDD 스킬 가이드 (공통 생성 가이드)

> 이 문서는 AI TDD 에이전트가 테스트 코드를 생성할 때 참조하는 **핵심 가이드**입니다.
> 클래스 유형 판별, 4단계 테스트 레벨, 규칙 적용, 검증 기준을 정의합니다.

---

## 1. 테스트 생성 프로세스

에이전트가 테스트 생성 요청을 받으면 다음 순서로 처리합니다.

```
1. 대상 소스 코드 분석
2. 클래스 유형 판별 (→ 템플릿 선택)
3. 프로젝트 설정 확인 (.claude.md)
4. 규칙 적용 (constraints/)
5. 예제 참조 (references/)
6. 4단계 레벨별 테스트 코드 생성
7. 검증 (verification/)
```

### 1.1. 클래스 유형 판별 기준

대상 클래스의 **어노테이션과 패턴**으로 유형을 판별하고, 해당 템플릿을 선택합니다.

| 판별 기준 | 유형 | 적용 템플릿 |
|---|---|---|
| `@Service`, `@Component` (비즈니스 로직) | Service | `templates/service-test.md` |
| `@RestController`, `@Controller` | Controller | `templates/controller-test.md` |
| `@Mapper`, MyBatis 인터페이스 | Mapper | `templates/mapper-test.md` |
| static 메서드 위주, Helper/Util 클래스 | Utility | `templates/util-test.md` |

**판별 우선순위**: 어노테이션 > 클래스명 패턴 > 패키지 경로
- 어노테이션이 없으면 클래스명(`*Service`, `*Controller` 등)으로 판별
- 그래도 불분명하면 패키지 경로(`service/`, `controller/` 등)로 판별

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

모든 테스트는 다음 4단계 레벨 비율로 생성합니다.

### Level 1 (40%): Happy Case - 정상 케이스

**목적**: 정상 입력으로 기대 결과가 나오는지 검증

| 항목 | 설명 |
|---|---|
| 대상 | 각 public 메서드의 정상 시나리오 |
| 패턴 | Given(정상 입력) → When(메서드 호출) → Then(기대 결과 검증) |

**생성 기준**:
- 모든 public 메서드에 대해 최소 1개 Happy Case 필수
- 반환값 검증 + 부수효과(DB insert, 외부 호출 등) verify
- `@DisplayName`에 한글로 시나리오 설명

**예시 패턴**:
```
"유효한 요청으로 사용자 생성 성공"
"정상 ID로 사용자 조회 성공"
"유효한 로그인 정보로 인증 성공"
```

### Level 2 (30%): Edge Case - 경계값/특수 입력

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
"ID가 null이면 InvalidUserIdException 발생"
"ID가 0이면 InvalidUserIdException 발생"
"이메일이 빈 문자열이면 예외 발생"
```

### Level 3 (20%): Exception - 비즈니스/시스템 예외

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

### Level 4 (10%): Mutation Testing - 숨겨진 버그/조건분기

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

## 3. 생성 결과물 표준 구조

생성되는 테스트 클래스는 다음 구조를 따릅니다.

```java
@ExtendWith(MockitoExtension.class)  // 단위테스트 기본
class {클래스명}Test {

    // ── Mock 선언 ──
    @Mock
    private {의존성타입} {의존성명};

    @InjectMocks
    private {대상클래스} {대상인스턴스};

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("한글 시나리오 설명")
    void should_{동작}_when_{조건}() {
        // Given
        // When
        // Then
    }

    // ── Level 2: Edge Case (30%) ──
    // ── Level 3: Exception (20%) ──
    // ── Level 4: Mutation Testing (10%) ──
}
```

### 3.1. 테스트 유형별 어노테이션

| 유형 | 어노테이션 | 용도 |
|---|---|---|
| Service 단위테스트 | `@ExtendWith(MockitoExtension.class)` | 의존성 Mock, 순수 단위테스트 |
| Controller 통합테스트 | `@WebMvcTest({Controller}.class)` | MockMvc 기반 HTTP 테스트 |
| Mapper 테스트 | `@MybatisTest` 또는 `@ExtendWith(MockitoExtension.class)` | DB 연동 또는 Mock |
| Utility 테스트 | 어노테이션 불필요 | static 메서드 직접 호출 |

### 3.2. 필수 검증 항목

모든 생성된 테스트에서 다음을 확인합니다.

| 항목 | 검증 방법 |
|---|---|
| 반환값 | `assertThat(result).isNotNull()`, `.isEqualTo()` 등 |
| 예외 | `assertThatThrownBy().isInstanceOf().hasMessage()` |
| 부수효과 | `verify({mock}).{메서드}()` |
| 호출 안됨 | `verify({mock}, never()).{메서드}()` |
| 호출 횟수 | `verify({mock}, times(N)).{메서드}()` |

---

## 4. NH 도메인 특화 검증

NH 프로젝트에서 반드시 확인해야 하는 도메인 규칙입니다.

| 규칙 | 테스트 검증 방법 |
|---|---|
| 개인정보 마스킹 | 주민번호, 카드번호 등 마스킹 처리 결과 검증 |
| Petra 암호화 | 비밀번호 등 암호화 메서드 호출 여부 verify |
| 감사로그 기록 | 핵심 비즈니스 작업 시 감사로그 기록 verify |
| 하드코딩 금지 | 테스트 데이터에 실제 개인정보/자격증명 사용 금지 |

---

## 5. 검증 프로세스

생성된 테스트 코드는 다음 3단계 검증을 통과해야 합니다.

### 5.1. 컴파일 검증

```bash
./gradlew compileTestJava
```
- **합격**: 컴파일 오류 0건
- **불합격 시**: import 누락, 타입 불일치, 메서드 시그니처 오류 수정

### 5.2. 실행 검증

```bash
./gradlew test
```
- **합격**: 모든 테스트 PASS
- **불합격 시**: Mock 설정, 어설션 값, 예외 타입 수정

### 5.3. 커버리지 검증

```bash
./gradlew test jacocoTestReport
```
- **합격 기준**: 라인 커버리지 80% 이상, 분기 커버리지 70% 이상
- **불합격 시**: 미커버 분기에 대한 추가 테스트 생성

> 상세 검증 절차는 `verification/` 폴더의 각 문서를 참조
