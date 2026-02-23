# 유틸리티 클래스 테스트 템플릿

> **대상**: static 메서드 위주 클래스, Helper, Util, Converter 등
> **테스트 방식**: 순수 단위테스트 (Spring 컨텍스트/Mock 불필요)

---

## 역할

당신은 유틸리티 클래스의 단위 테스트를 전문으로 하는 시니어 Java 테스트 엔지니어입니다.
JUnit 5와 AssertJ를 사용하여 static 메서드, 변환 로직, 헬퍼 함수를 검증하는 테스트를 생성합니다.

## 컨텍스트

다음은 테스트가 필요한 유틸리티 클래스입니다:

```java
[UTILITY_CLASS_CODE_HERE]
```

## 테스트 클래스 구조

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class {클래스명}Test {

    // ── Spring 컨텍스트, Mock 불필요 ──
    // ── static 메서드는 직접 호출 ──

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("한글로 시나리오 설명")
    void should_{동작}_when_{조건}() {
        // Given
        String input = "test-input";

        // When
        String result = {유틸클래스}.{메서드}(input);

        // Then
        assertThat(result).isEqualTo({기대값});
    }

    // ── Level 2: Edge Case (30%) - @ParameterizedTest 적극 활용 ──

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("null 또는 빈 문자열 입력 시 처리 검증")
    void should_{동작}_when_inputIsNullOrEmpty(String input) {
        // When & Then
        assertThatThrownBy(() -> {유틸클래스}.{메서드}(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({
        "input1, expected1",
        "input2, expected2",
        "input3, expected3"
    })
    @DisplayName("다양한 입력값에 대한 결과 검증")
    void should_{동작}_when_variousInputs(String input, String expected) {
        assertThat({유틸클래스}.{메서드}(input)).isEqualTo(expected);
    }

    // ── Level 3: Exception (20%) ──
    // ── Level 4: Mutation Testing (10%) ──
}
```

## 요구사항

### 기본 요구사항
- 유틸리티 클래스의 모든 public/static 메서드에 대한 테스트 생성
- Spring 컨텍스트 로드하지 않음 (어노테이션 불필요)
- Mock 사용 최소화 (순수 입출력 테스트)
- `@ParameterizedTest`를 적극 활용하여 다양한 입력 검증

### 4단계 레벨별 요구사항

**Level 1 (40%): Happy Case**
- 각 static 메서드의 정상 입출력 검증
- 예시: 문자열 변환 성공, 날짜 포맷팅 성공, 마스킹 처리 성공

**Level 2 (30%): Edge Case - `@ParameterizedTest` 적극 활용**
- null, empty, 공백, 특수문자, 최대 길이 등 경계값 테스트
- `@NullAndEmptySource`, `@ValueSource`, `@CsvSource` 활용
- 예시: null 입력 시 예외, 빈 문자열 시 빈 문자열 반환

**Level 3 (20%): Exception**
- 잘못된 입력에 대한 예외 처리 검증
- `assertThatThrownBy()` + `isInstanceOf()` + `hasMessage()` 사용
- 예시: 잘못된 포맷, 지원하지 않는 타입, 범위 초과

**Level 4 (10%): Mutation Testing**
- 조건분기의 양쪽 경로 모두 테스트
- 반환값의 정확한 형태 검증 (단순 not null이 아닌 정밀 비교)
- 예시: 마스킹 결과의 정확한 패턴 검증, 변환 결과의 모든 필드 검증

### 유틸리티 계층 특화 패턴

| 패턴 | 적용 대상 | JUnit 5 어노테이션 |
|---|---|---|
| 다중 입력값 테스트 | 같은 메서드에 여러 입력 | `@ParameterizedTest` + `@CsvSource` |
| null/empty 테스트 | null/빈값 처리 | `@ParameterizedTest` + `@NullAndEmptySource` |
| 다양한 단일값 | 단일 파라미터 여러 값 | `@ParameterizedTest` + `@ValueSource` |
| 메서드 소스 | 복잡한 객체 입력 | `@ParameterizedTest` + `@MethodSource` |
| private 생성자 | 유틸리티 클래스 인스턴스화 방지 | Reflection 기반 검증 (선택적) |

### private 생성자 테스트 (유틸리티 클래스의 경우)

```java
@Test
@DisplayName("유틸리티 클래스는 인스턴스화할 수 없음")
void should_throwException_when_instantiated() throws Exception {
    var constructor = {유틸클래스}.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    assertThatThrownBy(constructor::newInstance)
        .hasCauseInstanceOf(UnsupportedOperationException.class);
}
```

### 마스킹 유틸리티 테스트 (NH 도메인 특화)

```java
@ParameterizedTest
@CsvSource({
    "9001011234567, 900101-*******",
    "1234567890123456, ****-****-****-3456"
})
@DisplayName("개인정보 마스킹 패턴 검증")
void should_maskData_when_validInput(String input, String expected) {
    assertThat(MaskingUtil.mask(input)).isEqualTo(expected);
}
```

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test`
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명
- 레벨별 주석 구분: `// ── Level 1: Happy Case (40%) ──`
- `@ParameterizedTest`의 경우 `@DisplayName`에 입력 유형 명시

## 제약사항

- Spring 어노테이션(`@SpringBootTest`, `@ExtendWith` 등) 사용 금지
- 테스트 데이터에 실제 개인정보 사용 금지
- 마스킹/암호화 관련 유틸리티는 입출력 패턴 정밀 검증 필수
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조
