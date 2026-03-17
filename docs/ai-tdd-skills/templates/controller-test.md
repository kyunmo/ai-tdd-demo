# 컨트롤러 레이어 테스트 템플릿

> **대상**: `@RestController`, `@Controller` (REST API 클래스)
> **테스트 방식**: 슬라이스 테스트 (`@WebMvcTest`)

---

## 역할

당신은 Spring Boot REST 컨트롤러의 슬라이스 테스트를 전문으로 하는 시니어 Java 테스트 엔지니어입니다.
JUnit 5, MockMvc, Mockito, AssertJ를 사용하여 HTTP 요청/응답을 검증하는 테스트를 생성합니다.

## 컨텍스트

다음은 테스트가 필요한 REST 컨트롤러 클래스입니다:

```java
[CONTROLLER_CLASS_CODE_HERE]
```

## 테스트 클래스 구조

```java
package {패키지경로};

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

/**
 * {클래스명} 테스트 클래스
 * - Happy Cases : N개
 * - Edge Cases : N개
 * - Exception Cases : N개
 * - Mutation testing : N개
 */
@WebMvcTest({컨트롤러클래스}.class)
@AutoConfigureMockMvc(addFilters = false)
class {클래스명}Test {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    // 서비스 의존성은 @MockBean으로 선언
    @MockBean
    private {서비스타입} {서비스명};

    // Security 의존성 MockBean (필수 — 프로젝트의 Security 관련 Bean을 선언)
    // 프로젝트별로 Security 구성에 맞게 수정 필요
    // 예: JwtTokenProvider, JwtAuthenticationFilter, PasswordEncoder 등
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private PasswordEncoder passwordEncoder;

    // 테스트 데이터 상수
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_NAME = "테스트사용자";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String TEST_PHONE_NUMBER = "01012345678";

    /* Level 1: Happy Cases (각 메서드 1개) */
    @Nested
    @DisplayName("정상 케이스 테스트")
    class HappyCases {
        @Test
        @DisplayName("한글로 시나리오 설명")
        void should_{동작}_when_{조건}() throws Exception {
            // Given
            when({서비스}.{메서드}({인자})).thenReturn({반환값});
            
            // When & Then
            mockMvc.perform(get("/api/{경로}")
                    .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.{필드}").value({기대값}));
        }
    }    
    
    /* Level 2: Edge Case */
    @Nested
    @DisplayName("예외 상황 테스트")
    class EdgeCases {...}
    
    /* Level 3: Exception (throw 문 1:1) */
    @Nested
    @DisplayName("비즈니스 예외 테스트")
    class ExceptionCases {...}
    
    /* Level 4: Mutation Testing (각 메서드 1개 - 필수) */
    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {...}

    /* Helper Methods */

    private User createTestUser(Long id, String name, String email) {
        return User.builder()
            .id(id)
            .name(name)
            .email(email)
            .password(TEST_PASSWORD)
            .phoneNumber(TEST_PHONE_NUMBER)
            .build();
    }

    private CreateUserRequest createTestRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName(TEST_NAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setPhoneNumber(TEST_PHONE_NUMBER);
        return request;
    }
}
```

## 요구사항

### 기본 요구사항
- 컨트롤러의 모든 public 엔드포인트에 대한 슬라이스 테스트 생성
- `@WebMvcTest({Controller}.class)` + `@AutoConfigureMockMvc(addFilters = false)` 사용
- 서비스 의존성은 `@MockBean`으로 선언
- **Security 의존성도 @MockBean으로 선언** (필수)
  - `JwtTokenProvider`
  - `JwtAuthenticationFilter`
  - `PasswordEncoder`
- MockMvc를 사용한 HTTP 요청/응답 검증

### 4단계 레벨별 요구사항

**Level 1: Happy Case (각 메서드 1개)**
- 각 엔드포인트의 정상 요청/응답 시나리오
- HTTP 상태코드, 응답 본문, Content-Type 검증
- 예시: 정상 GET 요청 → 200 OK + JSON 응답

**Level 2: Edge Case**
- 잘못된 파라미터, 누락된 필수값, 빈 요청 본문
- 유효성 검증(@Valid) 실패 시나리오
- 예시: 필수 파라미터 누락 → 400 Bad Request

**Level 3: Exception (throw 문 1:1)**
- 서비스 계층 예외 발생시 HTTP 응답 검증
- 예시: 리소스 미존재 → 404 Not Found, 중복데이터 → 409 Conflict

**Level 4: Mutation Testing (각 메서드 1개 - 필수)**
- 서비스 메서드 호출 여부/인자 검증
- 응답 본문의 정확한 필드값 검증 (jsonPath)
- 예시: 서비스 메서드에 전달된 파라미터값 정밀 verify

```java
// 기본 서비스 호출 검증
verify(userService).createUser(any(CreateUserRequest.class));

// 파라미터 값 정밀 검증 (ArgumentCaptor 또는 argThat)
verify(userService).createUser(argThat(request ->
    request.getEmail().equals(TEST_EMAIL) &&
    request.getName().equals(TEST_NAME)
));
```

### 컨트롤러 계층 특화 패턴

| HTTP 메서드 | MockMvc 패턴 | 상태코드 |
|---|---|---|
| GET | `mockMvc.perform(get("/api/{경로}"))` | 200 OK |
| POST | `mockMvc.perform(post("/api/{경로}").content({JSON}).contentType(APPLICATION_JSON))` | 201 Created |
| PUT | `mockMvc.perform(put("/api/{경로}/{id}").content({JSON}).contentType(APPLICATION_JSON))` | 200 OK |
| DELETE | `mockMvc.perform(delete("/api/{경로}/{id}"))` | 204 No Content |

| 검증 대상 | MockMvc 검증 방법 |
|---|---|
| 상태코드 | `.andExpect(status().isOk())`, `.andExpect(status().isCreated())` |
| 응답 본문 (JSON) | `.andExpect(jsonPath("$.{필드}").value({값}))` |
| 응답 본문 (리스트) | `.andExpect(jsonPath("$.length()").value({개수}))` |
| Content-Type | `.andExpect(content().contentType(MediaType.APPLICATION_JSON))` |
| 서비스 호출 | `verify({서비스}).{메서드}({인자})` |

### JSON 요청 본문 작성

```java
// ObjectMapper를 사용한 JSON 변환
@Autowired
private ObjectMapper objectMapper;

String jsonContent = objectMapper.writeValueAsString(requestDto);

mockMvc.perform(post("/api/users"))
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON)
    .andExpect(status().isCreated());
```

## 생성 알고리즘

에이전트가 컨트롤러 소스 코드에서 테스트 코드를 기계적으로 변환하는 규칙입니다.

### @RequestMapping → MockMvc 호출 매핑 규칙

```
소스 어노테이션 → MockMvc 호출 변환:

@GetMapping("/{path}")          → mockMvc.perform(get("/api/{basePath}/{path}"))
@GetMapping("/{path}/{id}")     → mockMvc.perform(get("/api/{basePath}/{path}" + id))
@PostMapping("/{path}")         → mockMvc.perform(post("/api/{basePath}/{path}")
                                    .content(jsonContent).contentType(APPLICATION_JSON))
@PutMapping("/{path}/{id}")     → mockMvc.perform(put("/api/{basePath}/{path}" + id)
                                    .content(jsonContent).contentType(APPLICATION_JSON))
@DeleteMapping("/{path}/{id}")  → mockMvc.perform(delete("/api/{basePath}/{path}" + id))

basePath = 클래스 레벨 @RequestMapping의 value
```

### @RequestBody → JSON 요청 본문 생성 규칙

```
1. @RequestBody 파라미터 타입에서 필드 목록 추출
2. 각 필드에 더미 데이터 할당 (nh-rules 준수)
3. ObjectMapper.writeValueAsString()으로 JSON 변환

예시:
  소스: registerUser(@RequestBody UserRegistrationRequest request)
  UserRegistrationRequest 필드: name, email, password
  변환:
    UserRegistrationRequest request = new UserRegistrationRequest();
    request.setName("테스트사용자");
    request.setEmail("test@example.com");
    request.setPassword("testPassword");
    String jsonContent = objectMapper.writeValueAsString(request);
```

### HTTP 상태코드 매핑 규칙

| 소스 패턴 | 테스트 기대 상태코드 |
|---|---|
| `@PostMapping` + `@ResponseStatus(CREATED)` | `status().isCreated()` (201) |
| `@PostMapping` (기본) | `status().isOk()` (200) 또는 `isCreated()` (201) |
| `@GetMapping` | `status().isOk()` (200) |
| `@PutMapping` | `status().isOk()` (200) |
| `@DeleteMapping` + void 반환 | `status().isNoContent()` (204) |
| `@DeleteMapping` + 반환값 있음 | `status().isOk()` (200) |
| 서비스에서 `NotFoundException` throw | `status().isNotFound()` (404) |
| 서비스에서 `DuplicateException` throw | `status().isConflict()` (409) |
| `@Valid` 검증 실패 | `status().isBadRequest()` (400) |

### 서비스 의존성 → @MockBean 추출 규칙

```
컨트롤러의 생성자/필드 주입 서비스 → @MockBean으로 선언

예시:
  소스: public UserController(UserService userService)
  변환:
    @MockBean private UserService userService;
```

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test`
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명 (HTTP 메서드 + 경로 포함)
- 레벨별 주석 구분: `// Level 1: Happy Case (각 메서드 1개)`
- 모든 테스트에 `throws Exception` 선언

## 제약사항

- `@SpringBootTest` + `@AutoConfigureMockMvc` 사용 금지 → `@WebMvcTest` 사용
- 테스트 데이터에 실제 개인정보 사용 금지
- 비밀번호 관련 엔드포인트는 요청 데이터 마스킹 확인
- 인증이 필요한 엔드포인트는 `@WithMockUser` 사용 고려
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조

