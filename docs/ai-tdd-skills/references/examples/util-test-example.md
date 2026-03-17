# 유틸리티 테스트 예제

> `Utility` 클래스의 순수 단위 테스트 완성 예제
> 4단계 레벨(Happy/Edge/Exception/Mutation)을 모두 포함합니다.

---

## 1. 소스 클래스
```java
public class MaskingUtil {

    private MaskingUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            throw new IllegalArgumentException("전화번호는 null이거나 빈 문자열일 수 없습니다");
        }
        if (phoneNumber.length() == 11) {
            return phoneNumber.substring(0, 3) + "-****-" + phoneNumber.substring(7);
        } else if (phoneNumber.length() == 10) {
            return phoneNumber.substring(0, 3) + "-***-" + phoneNumber.substring(6);
        }
        throw new IllegalArgumentException("유효하지 않은 전화번호 형식: " + phoneNumber);
    }

    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("이메일은 null이거나 빈 문자열일 수 없습니다");
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식: " + email);
        }
        String localPart = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (localPart.length() <= 3) {
            return localPart + "****" + domain;
        }
        return localPart.substring(0, 3) + "****" + domain;
    }

    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("이름은 null이거나 빈 문자열일 수 없습니다");
        }
        if (name.length() == 1) {
            return name;
        }
        return name.charAt(0) + new String(new char[name.length() - 1]).replace('\0', '*');
    }
}
```

## 2. 테스트 클래스
```java
package com.nhcard.al.demo.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * MaskingUtil 테스트 클래스
 * - Happy Cases : 3개
 * - Edge Cases : 6개
 * - Exception Cases : 5개
 * - Mutation Testing : 4개
 */
class MaskingUtilTest {

    /* -------------------------------------------------
     * Level 1: Happy Cases (각 메서드 1개)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("정상 케이스 테스트")
    class HappyCases {

        @Test
        @DisplayName("11자리 전화번호 마스킹 성공")
        void should_maskPhoneNumber_when_validElevenDigits() {
            // Given
            String phoneNumber = "01012345678";

            // When
            String result = MaskingUtil.maskPhoneNumber(phoneNumber);

            // Then
            assertThat(result).isEqualTo("010-****-5678");
        }

        @Test
        @DisplayName("이메일 마스킹 성공")
        void should_maskEmail_when_validEmail() {
            // Given
            String email = "testuser@example.com";

            // When
            String result = MaskingUtil.maskEmail(email);

            // Then
            assertThat(result).isEqualTo("tes****@example.com");
            assertThat(result).doesNotContain("testuser");
        }

        @Test
        @DisplayName("이름 마스킹 성공")
        void should_maskName_when_validName() {
            // Given
            String name = "테스트사용자";

            // When
            String result = MaskingUtil.maskName(name);

            // Then
            assertThat(result).isEqualTo("테****");
            assertThat(result).doesNotContain("스트사용자");
        }
    }

    /* -------------------------------------------------
     * Level 2: Edge Cases — @ParameterizedTest 활용
     * ------------------------------------------------- */

    @Nested
    @DisplayName("경계값 테스트")
    class EdgeCases {

        @ParameterizedTest
        @CsvSource({
            "01012345678, 010-****-5678",
            "01098765432, 010-****-5432",
            "01112345678, 011-****-5678"
        })
        @DisplayName("다양한 11자리 전화번호 마스킹 검증")
        void should_maskPhoneNumber_when_variousElevenDigits(String input, String expected) {
            assertThat(MaskingUtil.maskPhoneNumber(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
            "0101234567, 010-***-4567",
            "0311234567, 031-***-4567"
        })
        @DisplayName("10자리 전화번호 마스킹 검증")
        void should_maskPhoneNumber_when_tenDigits(String input, String expected) {
            assertThat(MaskingUtil.maskPhoneNumber(input)).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
            "ab@example.com, ab****@example.com",
            "abc@example.com, abc****@example.com",
            "a@b.com, a****@b.com"
        })
        @DisplayName("이메일 로컬파트가 3자 이하일 때 마스킹 검증")
        void should_maskEmail_when_shortLocalPart(String input, String expected) {
            assertThat(MaskingUtil.maskEmail(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("1글자 이름은 그대로 반환")
        void should_returnAsIs_when_singleCharName() {
            // When
            String result = MaskingUtil.maskName("홍");

            // Then
            assertThat(result).isEqualTo("홍");
        }

        @ParameterizedTest
        @CsvSource({
            "홍길동, 홍**",
            "김수, 김*",
            "테스트사용자, 테****"
        })
        @DisplayName("다양한 이름 길이에 대한 마스킹 검증")
        void should_maskName_when_variousLengths(String input, String expected) {
            assertThat(MaskingUtil.maskName(input)).isEqualTo(expected);
        }

        @Test
        @DisplayName("유틸리티 클래스는 인스턴스화할 수 없음")
        void should_throwException_when_instantiated() throws Exception {
            // Given
            Constructor<?> constructor = MaskingUtil.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            // When & Then
            assertThatThrownBy(constructor::newInstance)
                    .hasCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    /* -------------------------------------------------
     * Level 3: Exception Cases (throw 문 1:1)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("예외 테스트")
    class ExceptionCases {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("전화번호가 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void should_throwException_when_phoneNumber_nullOrEmpty(String input) {
            assertThatThrownBy(() -> MaskingUtil.maskPhoneNumber(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("전화번호");
        }

        @ParameterizedTest
        @ValueSource(strings = {"123", "123456789012", "abcdefghijk"})
        @DisplayName("유효하지 않은 전화번호 형식이면 IllegalArgumentException 발생")
        void should_throwException_when_phoneNumber_invalidFormat(String input) {
            assertThatThrownBy(() -> MaskingUtil.maskPhoneNumber(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 전화번호 형식");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("이메일이 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void should_throwException_when_email_nullOrEmpty(String input) {
            assertThatThrownBy(() -> MaskingUtil.maskEmail(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이메일");
        }

        @Test
        @DisplayName("이메일에 @가 없으면 IllegalArgumentException 발생")
        void should_throwException_when_email_noAtSign() {
            assertThatThrownBy(() -> MaskingUtil.maskEmail("invalidemail"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 이메일 형식");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("이름이 null 또는 빈 문자열이면 IllegalArgumentException 발생")
        void should_throwException_when_name_nullOrEmpty(String input) {
            assertThatThrownBy(() -> MaskingUtil.maskName(input))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("이름");
        }
    }

    /* -------------------------------------------------
     * Level 4: Mutation Testing (각 메서드 1개 - 필수)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {

        @Test
        @DisplayName("전화번호 마스킹 결과에 원본 중간번호가 포함되지 않음")
        void should_notContainOriginalMiddle_when_maskPhoneNumber() {
            // Given
            String phoneNumber = "01012345678";

            // When
            String result = MaskingUtil.maskPhoneNumber(phoneNumber);

            // Then
            assertThat(result).doesNotContain("1234");
            assertThat(result).startsWith("010-");
            assertThat(result).endsWith("5678");
            assertThat(result).contains("****");
        }

        @Test
        @DisplayName("이메일 마스킹 결과에 원본 로컬파트가 포함되지 않음")
        void should_notContainOriginalLocal_when_maskEmail() {
            // Given
            String email = "longusername@example.com";

            // When
            String result = MaskingUtil.maskEmail(email);

            // Then
            assertThat(result).doesNotContain("longusername");
            assertThat(result).startsWith("lon");
            assertThat(result).contains("****");
            assertThat(result).endsWith("@example.com");
        }

        @Test
        @DisplayName("이름 마스킹 결과의 * 개수가 이름 길이 - 1과 일치")
        void should_haveCorrectAsterisks_when_maskName() {
            // Given
            String name = "홍길동";

            // When
            String result = MaskingUtil.maskName(name);

            // Then
            long asteriskCount = result.chars().filter(c -> c == '*').count();
            assertThat(asteriskCount).isEqualTo(name.length() - 1);
            assertThat(result.charAt(0)).isEqualTo('홍');
        }

        @Test
        @DisplayName("10자리와 11자리 전화번호의 마스킹 패턴이 다름")
        void should_haveDifferentPattern_when_tenVsElevenDigits() {
            // When
            String result11 = MaskingUtil.maskPhoneNumber("01012345678");
            String result10 = MaskingUtil.maskPhoneNumber("0101234567");

            // Then
            assertThat(result11).contains("-****-");  // 11자리: 4개 마스킹
            assertThat(result10).contains("-***-");   // 10자리: 3개 마스킹
        }
    }
}
```

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| 어노테이션 없음 | Spring 컨텍스트, Mock 불필요 (순수 단위 테스트) |
| `@ParameterizedTest` | `@CsvSource`, `@NullAndEmptySource`, `@ValueSource` 적극 활용 |
| `@DisplayName` 한글 | 시나리오를 한글로 명확히 설명 |
| `should_{동작}_when_{조건}` | 테스트 메서드 네이밍 규칙 준수 |
| Given-When-Then | 모든 테스트에 주석으로 구조 표시 |
| AssertJ | `assertThat()`, `assertThatThrownBy()` 사용 |

### 3.2. NH 도메인 규칙 적용

| 규칙 | 적용 위치 |
|---|---|
| PII 마스킹 검증 | 원본 데이터가 결과에 미포함 확인 (`doesNotContain`) |
| 마스킹 패턴 정밀 검증 | `startsWith`, `endsWith`, `contains("****")` |
| 안전한 테스트 데이터 | `"01012345678"`, `"test@example.com"`, `"테스트사용자"` 사용 |
| private 생성자 테스트 | `throw UnsupportedOperationException` → 패턴 A 적용 |

### 3.3. 유틸리티 테스트 특화 패턴

| 패턴 | 적용 예시 |
|---|---|
| `@ParameterizedTest` + `@CsvSource` | 다양한 입력-출력 쌍 검증 |
| `@ParameterizedTest` + `@NullAndEmptySource` | null/빈 문자열 일괄 검증 |
| `@ParameterizedTest` + `@ValueSource` | 다양한 잘못된 형식 일괄 검증 |
| private 생성자 테스트 | Reflection으로 인스턴스화 방지 확인 |
