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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@WebMvcTest({컨트롤러클래스}.class)
class {클래스명}Test {

    @Autowired
    private MockMvc mockMvc;

    // ── 서비스 의존성은 @MockBean으로 선언 ──
    @MockBean
    private {서비스타입} {서비스명};

    // ── Level 1: Happy Case (40%) ──

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

    // ── Level 2: Edge Case (30%) ──
    // ── Level 3: Exception (20%) ──
    // ── Level 4: Mutation Testing (10%) ──
}
```

## 요구사항

### 기본 요구사항
- 컨트롤러의 모든 public 엔드포인트에 대한 슬라이스 테스트 생성
- `@WebMvcTest({Controller}.class)` 사용 (컨트롤러 계층만 로드)
- 서비스 의존성은 `@MockBean`으로 선언
- MockMvc를 사용한 HTTP 요청/응답 검증

### 4단계 레벨별 요구사항

**Level 1 (40%): Happy Case**
- 각 엔드포인트의 정상 요청/응답 시나리오
- HTTP 상태코드, 응답 본문, Content-Type 검증
- 예시: 정상 GET 요청 → 200 OK + JSON 응답

**Level 2 (30%): Edge Case**
- 잘못된 파라미터, 누락된 필수값, 빈 요청 본문
- 유효성 검증(@Valid) 실패 시나리오
- 예시: 필수 파라미터 누락 → 400 Bad Request

**Level 3 (20%): Exception**
- 서비스 계층 예외 발생 시 HTTP 응답 검증
- 예시: 리소스 미존재 → 404 Not Found, 중복 데이터 → 409 Conflict

**Level 4 (10%): Mutation Testing**
- 서비스 메서드 호출 여부/인자 검증
- 응답 본문의 정확한 필드값 검증 (jsonPath)
- 예시: 서비스 메서드에 전달된 파라미터값 정밀 verify

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

mockMvc.perform(post("/api/users")
        .content(jsonContent)
        .contentType(MediaType.APPLICATION_JSON))
    .andExpect(status().isCreated());
```

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test`
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명 (HTTP 메서드 + 경로 포함)
- 레벨별 주석 구분: `// ── Level 1: Happy Case (40%) ──`
- 모든 테스트에 `throws Exception` 선언

## 제약사항

- `@SpringBootTest` + `@AutoConfigureMockMvc` 사용 금지 → `@WebMvcTest` 사용
- 테스트 데이터에 실제 개인정보 사용 금지
- 비밀번호 관련 엔드포인트는 요청 데이터 마스킹 확인
- 인증이 필요한 엔드포인트는 `@WithMockUser` 사용 고려
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조
