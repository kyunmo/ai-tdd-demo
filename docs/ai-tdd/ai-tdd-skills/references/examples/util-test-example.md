# 유틸리티 테스트 예제

> 유틸리티 클래스 (`static` 메서드)의 테스트 예제입니다.
> Spring 어노테이션 없이 순수 JUnit 5로 작성하며, `@ParameterizedTest`를 적극 활용합니다.
> NH 도메인의 **마스킹 유틸리티**를 예제로 사용합니다.

---

## 1. 소스 클래스

```java
public class MaskingUtil {

    private MaskingUtil() {
        // 인스턴스 생성 방지
    }

    /**
     * 주민등록번호 마스킹
     * 입력: "9001011234567" → 출력: "900101-*******"
     */
    public static String maskResidentNumber(String residentNumber) {
        if (residentNumber == null || residentNumber.length() != 13) {
            throw new IllegalArgumentException("주민등록번호는 13자리여야 합니다");
        }
        return residentNumber.substring(0, 6) + "-*******";
    }

    /**
     * 카드번호 마스킹
     * 입력: "1234567890123456" → 출력: "****-****-****-3456"
     */
    public static String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            throw new IllegalArgumentException("카드번호는 16자리여야 합니다");
        }
        return "****-****-****-" + cardNumber.substring(12);
    }

    /**
     * 휴대폰번호 마스킹
     * 입력: "01012345678" → 출력: "010-****-5678"
     */
    public static String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 10 || phoneNumber.length() > 11) {
            throw new IllegalArgumentException("휴대폰번호는 10~11자리여야 합니다");
        }
        String last4 = phoneNumber.substring(phoneNumber.length() - 4);
        String first3 = phoneNumber.substring(0, 3);
        return first3 + "-****-" + last4;
    }

    /**
     * 이메일 마스킹
     * 입력: "hong@nhbank.com" → 출력: "h***@nhbank.com"
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("유효하지 않은 이메일 형식입니다");
        }
        int atIndex = email.indexOf("@");
        String id = email.substring(0, atIndex);
        String domain = email.substring(atIndex);

        if (id.length() <= 1) {
            return id + "***" + domain;
        }
        return id.charAt(0) + "***" + domain;
    }
}
```

---

## 2. 테스트 클래스

```java
import static org.assertj.core.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MaskingUtilTest {

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("주민등록번호 정상 마스킹")
    void should_maskResidentNumber_when_validInput() {
        // When
        String result = MaskingUtil.maskResidentNumber("9001011234567");

        // Then
        assertThat(result).isEqualTo("900101-*******");
        assertThat(result).doesNotContain("1234567");
    }

    @Test
    @DisplayName("카드번호 정상 마스킹")
    void should_maskCardNumber_when_validInput() {
        // When
        String result = MaskingUtil.maskCardNumber("1234567890123456");

        // Then
        assertThat(result).isEqualTo("****-****-****-3456");
        assertThat(result).doesNotContain("1234567890");
    }

    @Test
    @DisplayName("휴대폰번호 정상 마스킹 (11자리)")
    void should_maskPhoneNumber_when_valid11Digits() {
        // When
        String result = MaskingUtil.maskPhoneNumber("01012345678");

        // Then
        assertThat(result).isEqualTo("010-****-5678");
        assertThat(result).doesNotContain("1234");
    }

    @Test
    @DisplayName("이메일 정상 마스킹")
    void should_maskEmail_when_validInput() {
        // When
        String result = MaskingUtil.maskEmail("hong@nhbank.com");

        // Then
        assertThat(result).isEqualTo("h***@nhbank.com");
        assertThat(result).doesNotContain("hong");
    }

    // ── Level 2: Edge Case (30%) ──

    @ParameterizedTest
    @DisplayName("다양한 주민등록번호 마스킹")
    @CsvSource({
        "9001011234567, 900101-*******",
        "0501012345678, 050101-*******",
        "8812311234567, 881231-*******"
    })
    void should_maskResidentNumber_when_variousInputs(String input, String expected) {
        // When
        String result = MaskingUtil.maskResidentNumber(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("다양한 카드번호 마스킹")
    @CsvSource({
        "1234567890123456, ****-****-****-3456",
        "9876543210987654, ****-****-****-7654",
        "1111222233334444, ****-****-****-4444"
    })
    void should_maskCardNumber_when_variousInputs(String input, String expected) {
        // When
        String result = MaskingUtil.maskCardNumber(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("휴대폰번호 10자리/11자리 모두 마스킹")
    @CsvSource({
        "01012345678, 010-****-5678",
        "0101234567, 010-****-4567"
    })
    void should_maskPhoneNumber_when_variousLengths(String input, String expected) {
        // When
        String result = MaskingUtil.maskPhoneNumber(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @ParameterizedTest
    @DisplayName("다양한 이메일 마스킹")
    @CsvSource({
        "hong@nhbank.com, h***@nhbank.com",
        "test@example.com, t***@example.com",
        "ab@domain.co.kr, a***@domain.co.kr"
    })
    void should_maskEmail_when_variousInputs(String input, String expected) {
        // When
        String result = MaskingUtil.maskEmail(input);

        // Then
        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("이메일 ID가 1자리이면 그대로 + *** 처리")
    void should_maskEmail_when_singleCharId() {
        // When
        String result = MaskingUtil.maskEmail("a@example.com");

        // Then
        assertThat(result).isEqualTo("a***@example.com");
    }

    // ── Level 3: Exception (20%) ──

    @ParameterizedTest
    @DisplayName("주민등록번호가 null이거나 빈 문자열이면 예외 발생")
    @NullAndEmptySource
    void should_throwException_when_residentNumberIsNullOrEmpty(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskResidentNumber(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @DisplayName("주민등록번호 자릿수가 맞지 않으면 예외 발생")
    @ValueSource(strings = {"123456", "12345678901234", "900101"})
    void should_throwException_when_residentNumberLengthInvalid(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskResidentNumber(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("주민등록번호는 13자리여야 합니다");
    }

    @ParameterizedTest
    @DisplayName("카드번호가 null이거나 빈 문자열이면 예외 발생")
    @NullAndEmptySource
    void should_throwException_when_cardNumberIsNullOrEmpty(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskCardNumber(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @DisplayName("카드번호 자릿수가 맞지 않으면 예외 발생")
    @ValueSource(strings = {"123456789012", "12345678901234567"})
    void should_throwException_when_cardNumberLengthInvalid(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskCardNumber(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("카드번호는 16자리여야 합니다");
    }

    @ParameterizedTest
    @DisplayName("휴대폰번호 자릿수 범위 밖이면 예외 발생")
    @ValueSource(strings = {"123456789", "123456789012"})
    void should_throwException_when_phoneNumberLengthInvalid(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskPhoneNumber(input))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("휴대폰번호는 10~11자리여야 합니다");
    }

    @Test
    @DisplayName("이메일에 @ 없으면 예외 발생")
    void should_throwException_when_emailHasNoAt() {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskEmail("invalid-email"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("유효하지 않은 이메일 형식입니다");
    }

    @ParameterizedTest
    @DisplayName("이메일이 null이거나 빈 문자열이면 예외 발생")
    @NullAndEmptySource
    void should_throwException_when_emailIsNullOrEmpty(String input) {
        // When & Then
        assertThatThrownBy(() -> MaskingUtil.maskEmail(input))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Level 4: Mutation Testing (10%) ──

    @Test
    @DisplayName("주민등록번호 마스킹 결과에 원본 뒷자리가 포함되지 않음")
    void should_notContainOriginalSuffix_when_maskResidentNumber() {
        // Given
        String original = "9001011234567";

        // When
        String result = MaskingUtil.maskResidentNumber(original);

        // Then - 원본 뒷자리 미포함 + 마스킹 패턴 확인
        assertThat(result).doesNotContain("1234567");
        assertThat(result).contains("*******");
        assertThat(result).hasSize(14); // "900101-*******" = 14자
        assertThat(result).startsWith("900101");
        assertThat(result).endsWith("*******");
    }

    @Test
    @DisplayName("카드번호 마스킹 결과에 앞 12자리가 포함되지 않음")
    void should_notContainFirst12Digits_when_maskCardNumber() {
        // Given
        String original = "1234567890123456";

        // When
        String result = MaskingUtil.maskCardNumber(original);

        // Then
        assertThat(result).doesNotContain("123456789012");
        assertThat(result).startsWith("****-****-****-");
        assertThat(result).endsWith("3456");
    }

    @Test
    @DisplayName("이메일 마스킹 결과에 원본 아이디가 포함되지 않음")
    void should_notContainOriginalId_when_maskEmail() {
        // Given
        String original = "hong@nhbank.com";

        // When
        String result = MaskingUtil.maskEmail(original);

        // Then
        assertThat(result).doesNotContain("hong");
        assertThat(result).contains("@nhbank.com"); // 도메인은 유지
        assertThat(result).startsWith("h"); // 첫 글자는 유지
    }

    @Test
    @DisplayName("유틸리티 클래스의 private 생성자 확인")
    void should_havePrivateConstructor() throws Exception {
        // Given
        Constructor<MaskingUtil> constructor = MaskingUtil.class.getDeclaredConstructor();

        // When & Then - private 접근 제한 확인 + 인스턴스 생성 가능 여부 확인
        assertThat(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers())).isTrue();
        constructor.setAccessible(true);
        assertThat(constructor.newInstance()).isNotNull();
    }
}
```

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| Spring 어노테이션 없음 | `@ExtendWith`, `@SpringBootTest` 등 불필요 |
| Mock 없음 | static 메서드이므로 Mock 불필요 |
| `@ParameterizedTest` | 여러 입력값에 대한 반복 테스트 |
| `@CsvSource` | 입력-출력 쌍을 CSV 형식으로 제공 |
| `@NullAndEmptySource` | null과 빈 문자열 테스트 |
| `@ValueSource` | 여러 잘못된 입력값 테스트 |

### 3.2. NH 도메인 규칙 적용

| 규칙 | 적용 위치 |
|---|---|
| 주민번호 마스킹 검증 | 뒷 7자리 `*******` 처리 + 원본 미포함 확인 |
| 카드번호 마스킹 검증 | 앞 12자리 `****-****-****-` 처리 + 원본 미포함 확인 |
| 휴대폰번호 마스킹 검증 | 중간 4자리 `****` 처리 |
| 이메일 마스킹 검증 | 아이디 부분 `***` 처리 + 도메인 유지 확인 |
| 안전한 테스트 데이터 | 명백한 더미 데이터만 사용 (`"9001011234567"` 등) |

### 3.3. 4단계 레벨 분포

| 레벨 | 테스트 수 | 핵심 패턴 |
|---|---|---|
| Level 1: Happy Case | 4개 | 각 메서드의 정상 입력 → 정상 출력 |
| Level 2: Edge Case | 6개 | `@ParameterizedTest` + `@CsvSource`로 다양한 입력 |
| Level 3: Exception | 7개 | `@NullAndEmptySource`, `@ValueSource`로 비정상 입력 |
| Level 4: Mutation | 4개 | 원본 데이터 미포함 확인, 패턴 매칭, private 생성자 |

> 이 예제는 `@ParameterizedTest`의 다양한 패턴을 보여주기 위해 Level 2~3 비중이 목표(40/30/20/10)보다 높습니다.
> 실제 생성 시 소스 코드의 메서드 수에 따라 비율을 40-30-20-10에 가깝게 조정합니다.

### 3.4. @ParameterizedTest 사용 패턴 요약

| 어노테이션 | 용도 | 예시 |
|---|---|---|
| `@CsvSource` | 입력-출력 쌍 | `"9001011234567, 900101-*******"` |
| `@NullAndEmptySource` | null + "" 테스트 | 필수 파라미터 검증 |
| `@ValueSource` | 잘못된 입력값 목록 | `strings = {"123456", "12345678901234"}` |
