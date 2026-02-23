# 컨트롤러 테스트 예제

> `@RestController` 클래스의 슬라이스 테스트 완성 예제입니다.
> `@WebMvcTest` + `MockMvc`를 사용하며, 4단계 레벨을 모두 포함합니다.

---

## 1. 소스 클래스

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDetailResponse> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(UserDetailResponse.from(user));
    }

    @GetMapping
    public ResponseEntity<List<UserDetailResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserDetailResponse> response = users.stream()
            .map(UserDetailResponse::from)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<UserDetailResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDetailResponse.from(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 2. 테스트 클래스

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("GET /api/users/{id} 정상 조회 시 200 OK")
    void should_returnUser_when_validId() throws Exception {
        // Given
        Long userId = 1L;
        User user = new User(userId, "테스트사용자", "test@example.com", "encryptedValue");
        when(userService.getUserById(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("테스트사용자"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("GET /api/users 전체 목록 조회 시 200 OK")
    void should_returnAllUsers_when_called() throws Exception {
        // Given
        List<User> users = List.of(
            new User(1L, "사용자1", "user1@example.com", "enc1"),
            new User(2L, "사용자2", "user2@example.com", "enc2")
        );
        when(userService.getAllUsers()).thenReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].name").value("사용자1"))
            .andExpect(jsonPath("$[1].name").value("사용자2"));
    }

    @Test
    @DisplayName("POST /api/users 정상 생성 시 201 Created")
    void should_createUser_when_validRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        User createdUser = new User(1L, "테스트사용자", "test@example.com", "encryptedValue");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("테스트사용자"))
            .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} 정상 삭제 시 204 No Content")
    void should_deleteUser_when_validId() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId))
            .andDo(print())
            .andExpect(status().isNoContent());
    }

    // ── Level 2: Edge Case (30%) ──

    @Test
    @DisplayName("GET /api/users 사용자 없으면 빈 배열 200 OK")
    void should_returnEmptyList_when_noUsersExist() throws Exception {
        // Given
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("POST /api/users 필수 필드 누락 시 400 Bad Request")
    void should_return400_when_requiredFieldMissing() throws Exception {
        // Given - name이 null인 요청
        String invalidJson = "{\"email\":\"test@example.com\",\"password\":\"testPassword\"}";

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/users 이메일 형식 오류 시 400 Bad Request")
    void should_return400_when_invalidEmailFormat() throws Exception {
        // Given
        String invalidJson = "{\"name\":\"테스트사용자\",\"email\":\"invalid-email\",\"password\":\"testPassword\"}";

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    // ── Level 3: Exception (20%) ──

    @Test
    @DisplayName("GET /api/users/{id} 존재하지 않는 ID이면 404 Not Found")
    void should_return404_when_userNotFound() throws Exception {
        // Given
        when(userService.getUserById(999L))
            .thenThrow(new UserNotFoundException("사용자를 찾을 수 없습니다: 999"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/users 중복 이메일이면 409 Conflict")
    void should_return409_when_emailIsDuplicate() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userService.createUser(any(CreateUserRequest.class)))
            .thenThrow(new DuplicateEmailException("이미 존재하는 이메일입니다"));

        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/users/{id} 유효하지 않은 ID이면 400 Bad Request")
    void should_return400_when_invalidId() throws Exception {
        // Given
        when(userService.getUserById(-1L))
            .thenThrow(new InvalidUserIdException("유효하지 않은 사용자 ID입니다"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", -1L)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    // ── Level 4: Mutation Testing (10%) ──

    @Test
    @DisplayName("GET /api/users/{id} 호출 시 서비스의 getUserById가 정확한 ID로 호출됨")
    void should_callServiceWithExactId_when_getUserById() throws Exception {
        // Given
        Long userId = 1L;
        User user = new User(userId, "테스트사용자", "test@example.com", "encryptedValue");
        when(userService.getUserById(userId)).thenReturn(user);

        // When
        mockMvc.perform(get("/api/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        // Then - 정확한 파라미터로 서비스 호출 검증
        verify(userService).getUserById(eq(1L));
        verify(userService, times(1)).getUserById(anyLong());
    }

    @Test
    @DisplayName("DELETE /api/users/{id} 호출 시 서비스의 deleteUser가 정확한 ID로 호출됨")
    void should_callServiceWithExactId_when_deleteUser() throws Exception {
        // Given
        Long userId = 1L;
        doNothing().when(userService).deleteUser(userId);

        // When
        mockMvc.perform(delete("/api/users/{id}", userId))
            .andExpect(status().isNoContent());

        // Then
        verify(userService).deleteUser(eq(1L));
        verify(userService, times(1)).deleteUser(anyLong());
    }

    @Test
    @DisplayName("POST /api/users 응답 본문에 비밀번호가 포함되지 않음")
    void should_notExposePassword_when_creatingUser() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        User createdUser = new User(1L, "테스트사용자", "test@example.com", "encryptedValue");
        when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

        // When & Then - 응답에 비밀번호 필드가 없는지 확인
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.password").doesNotExist())
            .andExpect(jsonPath("$.name").value("테스트사용자"));
    }
}
```

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| `@WebMvcTest(UserController.class)` | 컨트롤러 슬라이스 테스트 (Service/Mapper 로드 안 함) |
| `@MockBean` | 컨트롤러가 의존하는 서비스를 Mock 처리 |
| `MockMvc` | HTTP 요청/응답 시뮬레이션 |
| `ObjectMapper` | JSON 요청 본문 직렬화 |
| `jsonPath()` | 응답 JSON 필드별 검증 |

### 3.2. HTTP 상태코드 매핑

| HTTP 메서드 | 정상 상태 | 오류 상태 |
|---|---|---|
| GET (단건) | 200 OK | 404 Not Found, 400 Bad Request |
| GET (목록) | 200 OK | - |
| POST | 201 Created | 400 Bad Request, 409 Conflict |
| DELETE | 204 No Content | 404 Not Found |

### 3.3. NH 도메인 규칙 적용

| 규칙 | 적용 위치 |
|---|---|
| 비밀번호 미노출 | `should_notExposePassword_when_creatingUser` - 응답에 password 필드 없음 확인 |
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"`, `"testPassword"` 사용 |

### 3.4. 4단계 레벨 분포

| 레벨 | 테스트 수 | 테스트 메서드 |
|---|---|---|
| Level 1: Happy Case | 4개 | GET(단건), GET(목록), POST, DELETE |
| Level 2: Edge Case | 3개 | 빈 목록, 필수필드 누락, 이메일 형식 오류 |
| Level 3: Exception | 3개 | 404 Not Found, 409 Conflict, 400 Invalid ID |
| Level 4: Mutation | 3개 | 서비스 호출 파라미터 검증(2), 비밀번호 미노출 |
