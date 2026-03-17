# 서비스 레이어 테스트 템플릿

> **대상**: `@Service`, `@Component` (비즈니스 로직 클래스)
> **테스트 방식**: 순수 단위테스트 (`@ExtendWith(MockitoExtension.class)`)
 
---

## 역할

당신은 Spring Boot 서비스 계층의 단위 테스트를 전문으로 하는 시니어 Java 테스트 엔지니어입니다.
JUnit 5, Mockito, AssertJ를 사용하여 비즈니스 로직을 검증하는 테스트를 생성합니다.

## **주의사항** (지속적인 테스트로 도출된 개선사항임)

- 코드 복잡도에 따라 필요한 import 요소가 누락되거나 불필요하게 추가되지 않도록 더블 체크 필요함
- 생성시 Edge Cases와 Exception Case 중복 테스트 방지
- null 검증시 테스트를 NPE Expected로 변경 또는 코드에 null 검증 추가

## 테스트 클래스 구조

```java
package {패키지경로};

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * {클래스명} 테스트 클래스
 * - Happy Cases : N개
 * - Edge Cases : N개
 * - Exception Cases : N개
 * - Mutation testing : N개
 */
@ExtendWith(MockitoExtension.class)
class {클래스명}Test {
    
    // Mock 선언: 대상 클래스의 모든 의존성을 @Mock으로 선언
    @Mock
    private {의존성타입} {의존성명};
    
    // 테스트 대상
    @InjectMocks
    private {서비스클래스} {인스턴스명};

    // 테스트 데이터 상수
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_NAME = "테스트사용자";

    /* Level 1: Happy Cases (각 메서드 1개) */
    @Nested
    @DisplayName("정상 케이스 테스트")
    class HappyCases {
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
    }

    /* Level 2: Edge Cases */
    @Nested
    @DisplayName("예외 상황 테스트")
    class EdgeCases {...}

    /* Level 3: Exception Cases (throw 문 1:1) */
    @Nested
    @DisplayName("비즈니스 예외 테스트")
    class ExceptionCases {...}

    /* Level 4: Mutation Testing (각 메서드 1개 - 필수) */
    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {...}

    /* Helper Methods */
}
```

## 요구사항

### 기본 요구사항
- 서비스 클래스의 모든 public 메서드에 대한 단위 테스트 생성
- `@ExtendWith(MockitoExtension.class)` 사용 (Spring 컨텍스트 로드하지 않음)
- 모든 의존성은 `@Mock`으로 선언, 대상 클래스는 `@InjectMocks`
- Given-When-Then 패턴 적용

### 4단계 레벨별 요구사항

**Level 1: Happy Case (각 메서드 1개)**
- 각 public 메서드마다 정상 시나리오 1개
- 반환값 검증 (`assertThat`)
- 예시: 유효한 요청으로 데이터 생성/조회/수정/삭제 성공

**Level 2: Edge Case**
- 파라미터 null, empty, 0, 음수, 경계값 테스트
- 예시: `null` 파라미터, 빈 문자열, ID가 0 또는 음수

**Level 3: Exception (throw 문 1:1)**
- 소스 코드의 모든 throw 문에 대한 테스트
- `assertThatThrownBy()` 사용, 예외 타입 + 메시지 모두 검증
- 예시: 중복 데이터, 미존재 데이터, 비즈니스 규칙 위반

**Level 4: Mutation Testing (각 메서드 1개 - 필수)**
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

## 생성 알고리즘

에이전트가 서비스 소스 코드에서 테스트 코드를 기계적으로 변환하는 규칙입니다.

### 생성자 파라미터 → @Mock 추출 규칙

```
입력: 소스 클래스의 생성자(또는 @RequiredArgsConstructor 필드)

변환 규칙:
1. 생성자의 각 파라미터 → @Mock private {타입} {이름};
2. 대상 클래스 → @InjectMocks private {클래스} {camelCase(클래스명)};

예시:
  소스: public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder)
  변환:
    @Mock private UserMapper userMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;
```

### 메서드 시그니처 → 테스트 메서드 생성 규칙

```
각 public 메서드에 대해:

1. Happy Case (Level 1):
   - 모든 의존성 Mock에 when().thenReturn() 설정
   - 메서드 호출 후 변환값 검증
   - 부수효과 verify() 추가

2. Edge Case (Level 2):
   - 각 파라미터의 타입별 Edge Case 매트릭스 적용 (SKILL.md 3.1. 참조)

3. Exception (Level 3):
   - 소스의 각 throw 문에 대해:
     a. throw 조건을 유발하는 Mock 설정 (when().thenReturn() 또는 thenThrow())
     b. assertThatThrownBy()로 예외 타입 + 메시지 검증

4. Mutation (Level 4):
   - 도메인 로직 호출 verify (암호화, 감사로그 등)
   - 호출 순서가 중요한 경우 InOrder 검증
   - never() 검증 (예외 시 후속 메서드 미호출 등)
```

### void 메서드 테스트 패턴

```java
// void 메서드는 반환값 검증 대신 부수효과 검증
@Test
@DisplayName("한글 시나리오 설명")
void should_{동작}_when_{조건}() {
    // Given
    when({mock}.{조회메서드}({인자})).thenReturn({데이터});

    // When
    {인스턴스}.{void메서드}({인자});
    
    // Then - 부수효과 검증
    verify({mock}).{부수효과메서드}({인자});
    verify({mock}, never()).{호출안되어야할메서드}(any());
}
```

### 위임 메서드 테스트 패턴

```
소스에서 다른 메서드를 호출하는 경우 (예: deleteUser → getUserById);
- 위임 대상 메서드의 예외가 전파되는지 검증
- 위임 메서드가 호출되었는지 verify (간접 검증)
```

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test`
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: **한글로 시나리오 설명**
- 레벨별 주석 구분: `// Level 1: Happy Case (각 메서드 1개)`
- 각 테스트에 Given-When-Then 주석 (필수)

## 제약사항

- `@SpringBootTest` 사용 금지 (서비스 단위테스트에 불필요)
- 테스트 데이터에 실제 개인정보(주민번호, 카드번호 등) 사용 금지
- 비밀번호 관련 로직은 Petra 암호화 호출 verify 포함
- 감사로그 기록 메서드가 있으면 호출 여부 verify 포함
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조