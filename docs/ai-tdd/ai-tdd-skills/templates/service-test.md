# 서비스 레이어 테스트 템플릿

> **대상**: `@Service`, `@Component` (비즈니스 로직 클래스)
> **테스트 방식**: 순수 단위테스트 (`@ExtendWith(MockitoExtension.class)`)

---

## 역할

당신은 Spring Boot 서비스 계층의 단위 테스트를 전문으로 하는 시니어 Java 테스트 엔지니어입니다.
JUnit 5, Mockito, AssertJ를 사용하여 비즈니스 로직을 검증하는 테스트를 생성합니다.

## 컨텍스트

다음은 테스트가 필요한 서비스 클래스입니다:

```java
[SERVICE_CLASS_CODE_HERE]
```

## 테스트 클래스 구조

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class {클래스명}Test {

    // ── Mock 선언: 대상 클래스의 모든 의존성을 @Mock으로 선언 ──
    @Mock
    private {의존성타입} {의존성명};

    // ── 테스트 대상 ──
    @InjectMocks
    private {서비스클래스} {인스턴스명};

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("한글로 시나리오 설명")
    void should_{동작}_when_{조건}() {
        // Given - 테스트 데이터 준비 및 Mock 설정
        when({mock}.{메서드}({인자})).thenReturn({반환값});

        // When - 테스트 대상 메서드 실행
        {반환타입} result = {인스턴스}.{메서드}({인자});

        // Then - 결과 검증
        assertThat(result).isNotNull();
        assertThat(result.get{필드}()).isEqualTo({기대값});
        verify({mock}).{메서드}({인자});
    }

    // ── Level 2: Edge Case (30%) ──
    // ── Level 3: Exception (20%) ──
    // ── Level 4: Mutation Testing (10%) ──
}
```

## 요구사항

### 기본 요구사항
- 서비스 클래스의 모든 public 메서드에 대한 단위 테스트 생성
- `@ExtendWith(MockitoExtension.class)` 사용 (Spring 컨텍스트 로드하지 않음)
- 모든 의존성은 `@Mock`으로 선언, 대상 클래스는 `@InjectMocks`
- Given-When-Then 패턴 적용

### 4단계 레벨별 요구사항

**Level 1 (40%): Happy Case**
- 각 public 메서드마다 정상 시나리오 최소 1개
- 반환값 검증 (`assertThat`) + 부수효과 검증 (`verify`)
- 예시: 유효한 요청으로 데이터 생성/조회/수정/삭제 성공

**Level 2 (30%): Edge Case**
- 파라미터 null, empty, 0, 음수, 경계값 테스트
- 예시: `null` 파라미터, 빈 문자열, ID가 0 또는 음수

**Level 3 (20%): Exception**
- 소스 코드의 모든 throw 문에 대한 테스트
- `assertThatThrownBy()` 사용, 예외 타입 + 메시지 모두 검증
- 예시: 중복 데이터, 미존재 데이터, 비즈니스 규칙 위반

**Level 4 (10%): Mutation Testing**
- `verify()`로 메서드 호출 여부/횟수/인자 정밀 검증
- `verify({mock}, never())`로 호출되면 안 되는 메서드 검증
- 조건분기의 양쪽 경로 모두 테스트
- 예시: 암호화 호출 검증, insert 호출 횟수 검증, 호출 순서 검증

### 서비스 계층 특화 패턴

| 패턴 | 검증 방법 |
|---|---|
| 의존성 호출 검증 | `verify({mock}).{메서드}()` |
| 호출 안됨 검증 | `verify({mock}, never()).{메서드}()` |
| 호출 횟수 검증 | `verify({mock}, times(N)).{메서드}()` |
| 호출 순서 검증 | `InOrder inOrder = inOrder({mock1}, {mock2})` |
| 인자 캡처 | `ArgumentCaptor<{타입}> captor = ArgumentCaptor.forClass({타입}.class)` |
| 예외 발생 시 Mock | `when({mock}.{메서드}()).thenThrow(new {예외}())` |

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test`
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명
- 레벨별 주석 구분: `// ── Level 1: Happy Case (40%) ──`
- 각 테스트에 Given-When-Then 주석

## 제약사항

- `@SpringBootTest` 사용 금지 (서비스 단위테스트에 불필요)
- 테스트 데이터에 실제 개인정보(주민번호, 카드번호 등) 사용 금지
- 비밀번호 관련 로직은 Petra 암호화 호출 verify 포함
- 감사로그 기록 메서드가 있으면 호출 여부 verify 포함
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조
