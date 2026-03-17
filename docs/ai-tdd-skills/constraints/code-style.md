# NH 코드 스타일 가이드라인

> 이 문서는 테스트 코드의 **구조, 포맷, 패턴**에 대한 스타일 가이드를 정의합니다.
> 모든 규칙에 좋은 예시와 나쁜 예시를 포함합니다.

---

## 1. 테스트 코드 구조

### 1.1. Given-When-Then 패턴

모든 테스트는 **Given-When-Then** 패턴으로 구조화합니다.

- **Given**: 테스트 데이터·상수·Mock 설정 (가능하면 `private static final` 상수 사용
- **When**: 대상 메서드 호출
- **Then**: `AssertJ` 로 **값 검증**(`assertThat(result).containsExactlyElementsOf(list)`, `usingRecursiveComparison()` 등) 및 **부수효과**(`verify`, `ArgumentCaptor`) 검증
- `// Given`, `// When`, `// Then` 주석은 **반드시** 포함합니다.

```java
// 좋은 예시
@Test
@DisplayName("유효한 요청으로 사용자 생성 성공")
void should_registerUser_when_validRequest() {
    // Given - 테스트 데이터 준비 및 Mock 설정
    UserRegistrationRequest request = new UserRegistrationRequest("테스트사용자", "testId", "testPassword");
    when(userMapper.findById(request.getUserId())).thenReturn(null);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    
    // When - 테스트 대상 메서드 실행
    User result = userService.registerUser(request);
    
    // Then - 결과 검증
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("테스트사용자");
    verify(userMapper).insert(any(User.class));
}

// 나쁜 예시
@Test
void testRegisterUser() {
    UserRegistrationRequest request = new UserRegistrationRequest("테스트사용자", "testId", "testPassword");
    when(userMapper.findById(request.getUserId())).thenReturn(null);
    when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
    User result = userService.registerUser(request);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo("테스트사용자");
    verify(userMapper).insert(any(User.class));
    // Given-When-Then 주석 없음. @DisplayName 없음
}
```

### 1.2. 예외 테스트 구조 (When & Then 통합)

예외를 검증하는 테스트는 **When & Then**을 통합합니다.

```java
// 좋은 예시
@Test
@DisplayName("중복 아이디면 BusinessException 발생")
void should_throwException_when_userIdIsDuplicate() {
    // Given
    UserRegistrationRequest request = new UserRegistrationRequest("테스트사용자", "testId", "testPassword");
    when(userMapper.findById(request.getUserId())).thenReturn(new User());
    
    // When & Then
    assertThatThrownBy(() -> userService.registerUser(request))
            .isInstanceOf(BusinessException.class)
            .hasMessage("이미 존재하는 아이디입니다.");
}

// 나쁜 예시 - try-catch 사용
@Test
void testDuplicateUserId() {
    try {
        userService.registerUser(request);
        fail("예외가 발생해야 합니다.");
    } catch (BusinessException e) {
        assertEquals("이미 존재하는 아이디입니다.", e.getMessage());
    }
}
```

### 1.3. 테스트 클래스 내부 구조

```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    // 1. Mock 선언 (필드)
    @Mock
    private UserMapper userMapper;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    // 2. 테스트 대상
    @InjectMocks
    private UserService userService;
    
    // 3. 공통 테스트 데이터 (필요시)
    // private static final String TEST_EMAIL = "test@example.com";
    
    // 4. 레벨별 테스트 메서드 (Level 1 → 2 → 3 → 4 순서)
    // Level 1: Happy Case (각 메서드 1개)
    // Level 2: Edge Case
    // Level 3: Exception (throw 문 1:1)
    // Level 4: Mutation Testing (각 메서드 1개 - 필수)
}
```

---

## 2. 어설션 스타일

### 2.1. AssertJ 사용 (필수)

JUnit 기본 assert 대신 **AssertJ**를 사용합니다.

```java
// 좋은 예시 - AssertJ
assertThat(result).isNotNull();
assertThat(result.getName()).isEqualTo("테스트사용자");
assertThat(result.getEmail()).contains("@");
assertThat(list).hasSize(3);
assertThat(list).containsExactly("a", "b", "c");

// 나쁜 예시 - JUnit 기본 assert
assertNotNull(result);
assertEquals("테스트사용자", result.getName());
assertTrue(result.getEmail().contains("@"));
assertEquals(3, list.size());
```
 
### 2.2. 예외 어설션

```java
// 좋은 예시 - assertThatThrownBy
assertThatThrownBy(() -> userService.getUserById(null))
        .isInstanceOf(InvalidUserIdException.class)
        .hasMessage("유효하지 않은 사용자 ID입니다.");

// 좋은 예시 - assertThatCode (예외 없음 확인)
assertThatCode(() -> userService.registerUser(validRequest))
        .doesNotThrowAnyException();

// 나쁜 예시 - @Test(expected=...)
@Test(expected = InvalidUserIdException.class)
void testInvalidId() { }

// 나쁜 예시 - assertThrows (JUnit 5 기본)
assertThrows(InvalidUserIdException.class, () -> userService.getUserById(null));
```

---

## 3. 일반 포맷 규칙

### 3.1. 기본 포맷

| 항목 | 규칙 |
|---|---|
| 들여쓰기 | 4칸 공백 (탭 금지) |
| 줄 길이 | 최대 120자 |
| 문장 | 한 줄에 하나 |
| 빈 줄 | 논리적 섹션 구분에 사용 |

### 3.2. import 순서

테스트 클래스의 import는 다음 순서로 정리합니다.

```java
// 1. static import (테스트 라이브러리)
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// 2. JUnit / 테스트 프레임워크
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;

// 3. Mockito
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// 4. Spring (필요시)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

// 5. 프로젝트 내부
import com.nhcard.al.demo.service.UserService;
import com.nhcard.al.demo.mapper.UserMapper;
```

### 3.3. 메서드 체이닝 포맷

```java
// 좋은 예시 - 체이닝은 줄바꿈 + 들여쓰기
assertThatThrownBy(() -> userService.registerUser(request))
        .isInstanceOf(BusinessException.class)
        .hasMessage("이미 존재하는 아이디입니다.");

mockMvc.perform(get("/api/users/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("테스트사용자"));

// 나쁜 예시 - 한 줄에 모두
assertThatThrownBy(() -> userService.registerUser(request)).isInstanceOf(BusinessException.class).hasMessage("이미 존재하는 아이디입니다.");
```

---

## 4. 테스트 작성 원칙

| 원칙 | 설명 |
|---|---|
| 하나의 테스트 = 하나의 검증 | 한 테스트에서 여러 시나리오를 검증하지 않음 |
| 독립적 실행 | 테스트 간 순서 의존성 없음. 공유 상태 없음 |
| 자기 설명적 | @DisplayName + Given-When-Then 주석으로 의도 명확 |
| 반복 가능 | 외부 상태에 의존하지 않음, 항상 같은 결과 |
| 빠른 실행 | Spring 컨텍스트 로드 최소화 (단위테스트 우선) |

---

## 5. 피해야 할 패턴

| 패턴 | 문제 | 대안 |
|---|---|---|
| `@SpringBootTest` (단위테스트) | 전체 컨텍스트 로드, 느림 | `@ExtendWith(MockitoExtension.class)` |
| `@MockBean` (Service 단위테스트에) | 컨텍스트 캐시 무효화 | `@Mock` + `@InjectMocks` |
| `Thread.sleep()` | 비결정적, 느림 | `await().atMost()` 또는 Mock |
| `System.out.println()` | 디버깅 잔재 | 로거 또는 제거 |
| `@Disabled` 남용 | 실패 테스트 은폐 | 수정 또는 삭제 |
| 비어있는 catch 블록 | 예외 무시 | `assertThatThrownBy()` 사용 |

> **주의**: `@WebMvcTest` Controller 테스트에서는 `@MockBean`이 **필수**입니다. 위 규칙은 Service/Mapper 단위테스트에만 적용됩니다.
 
