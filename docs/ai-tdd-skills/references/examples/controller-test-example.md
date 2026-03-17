# 컨트롤러 테스트 예제

> `Controller` 클래스의 단위 테스트 완성 예제
> 4단계 레벨(Happy/Edge/Exception/Mutation)을 모두 포함합니다.

---

## 1. 소스 클래스
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserController userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/detail/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping("/create")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        User created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/update/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserRequest request) {
        User updated = userService.updateUser(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/detail/{id}/masked-phone")
    public ResponseEntity<Map<String, String>> getMaskedPhoneNumber(@PathVariable Long id) {
        String masked = userService.getMaskedPhoneNumber(id);
        return ResponseEntity.ok(Map.of("maskedPhoneNumber", masked));
    }
}
```

## 2. 테스트 클래스
```java
package com.nhcard.al.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhcard.al.demo.domain.User;
import com.nhcard.al.demo.dto.CreateUserRequest;
import com.nhcard.al.demo.exception.DuplicateEmailException;
import com.nhcard.al.demo.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 테스트 클래스
 *
 * 4단계 테스트 레벨 적용:
 * - Happy Cases : N개
 * - Edge Cases : N개
 * - Exception Cases : N개
 * - Mutation testing : N개
 */
@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    // Security 의존성 MockBean (필수)
    @MockBean
    private com.nhcard.al.demo.security.JwtTokenProvider jwtTokenProvider;
    @MockBean
    private com.nhcard.al.demo.security.JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    // 테스트 데이터
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
        @DisplayName("GET /api/users/list - 전체 사용자 목록 조회 성공")
        void should_returnUserList_when_getAllUsers() throws Exception {
            // Given
            User user = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

            // When & Then
            mockMvc.perform(get("/api/users/list"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$[0].name").value(TEST_NAME));
        }

        @Test
        @DisplayName("GET /api/users/detail/{id} - 사용자 상세 조회 성공")
        void should_returnUser_when_getUserById() throws Exception {
            // Given
            User user = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.getUserById(TEST_USER_ID)).thenReturn(user);

            // When & Then
            mockMvc.perform(get("/api/users/detail/{id}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.name").value(TEST_NAME))
                    .andExpect(jsonPath("$.email").value(TEST_EMAIL));
        }

        @Test
        @DisplayName("POST /api/users/create - 사용자 생성 성공")
        void should_createUser_when_validRequest() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            User createdUser = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(TEST_USER_ID))
                    .andExpect(jsonPath("$.name").value(TEST_NAME));
        }

        @Test
        @DisplayName("POST /api/users/update/{id} - 사용자 수정 성공")
        void should_updateUser_when_validRequest() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            User updatedUser = createTestUser(TEST_USER_ID, "수정된이름", TEST_EMAIL);
            when(userService.updateUser(eq(TEST_USER_ID), any(CreateUserRequest.class))).thenReturn(updatedUser);

            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/update/{id}", TEST_USER_ID)
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("수정된이름"));
        }

        @Test
        @DisplayName("POST /api/users/delete/{id} - 사용자 삭제 성공")
        void should_deleteUser_when_validId() throws Exception {
            // Given
            doNothing().when(userService).deleteUser(TEST_USER_ID);

            // When & Then
            mockMvc.perform(post("/api/users/delete/{id}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("GET /api/users/detail/{id}/masked-phone - 마스킹된 전화번호 조회 성공")
        void should_returnMaskedPhone_when_getMaskedPhone() throws Exception {
            // Given
            String maskedPhone = "010-****-5678";
            when(userService.getMaskedPhoneNumber(TEST_USER_ID)).thenReturn(maskedPhone);

            // When & Then
            mockMvc.perform(get("/api/users/detail/{id}/masked-phone", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.maskedPhoneNumber").value(maskedPhone));
        }
    }

    /* Level 2: Edge Cases */

    @Nested
    @DisplayName("Edge Case 테스트")
    class EdgeCases {

        @Test
        @DisplayName("GET /api/users/list - 사용자 목록이 비어있을 때 빈 리스트 반환")
        void should_returnEmptyList_when_noUsers() throws Exception {
            // Given
            when(userService.getAllUsers()).thenReturn(Collections.emptyList());

            // When & Then
            mockMvc.perform(get("/api/users/list"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @DisplayName("POST /api/users/create - 이름이 null이면 400 Bad Request")
        void should_returnBadRequest_when_nameIsNull() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            request.setName(null);
            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/users/create - 이메일이 잘못된 형식이면 400 Bad Request")
        void should_returnBadRequest_when_invalidEmailFormat() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            request.setEmail("invalid-email");
            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/users/create - 비밀번호가 8자 미만이면 400 Bad Request")
        void should_returnBadRequest_when_passwordTooShort() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            request.setPassword("1234567"); // 7자
            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/users/create - 전화번호 형식이 잘못되면 400 Bad Request")
        void should_returnBadRequest_when_invalidPhoneFormat() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            request.setPhoneNumber("0212345678"); // 02로 시작 - 잘못된 형식
            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }

    /* Level 3: Exception Cases */

    @Nested
    @DisplayName("비즈니스 예외 테스트")
    class ExceptionCases {

        @Test
        @DisplayName("GET /api/users/detail/{id} - 사용자가 존재하지 않으면 404 Not Found")
        void should_returnNotFound_when_userNotExist() throws Exception {
            // Given
            when(userService.getUserById(TEST_USER_ID))
                    .thenThrow(new UserNotFoundException(TEST_USER_ID));

            // When & Then
            mockMvc.perform(get("/api/users/detail/{id}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/users/delete/{id} - 사용자가 존재하지 않으면 404 Not Found")
        void should_returnNotFound_when_deleteNonExistentUser() throws Exception {
            // Given
            doThrow(new UserNotFoundException(TEST_USER_ID))
                    .when(userService).deleteUser(TEST_USER_ID);

            // When & Then
            mockMvc.perform(post("/api/users/delete/{id}", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("POST /api/users/create - 중복 이메일로 생성 시 409 Conflict")
        void should_returnConflict_when_duplicateEmail() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            when(userService.createUser(any(CreateUserRequest.class)))
                    .thenThrow(new DuplicateEmailException(TEST_EMAIL));

            String jsonContent = objectMapper.writeValueAsString(request);

            // When & Then
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("GET /api/users/detail/{id}/masked-phone - 사용자가 존재하지 않으면 404 Not Found")
        void should_returnNotFound_when_getMaskedPhoneUserNotExist() throws Exception {
            // Given
            when(userService.getMaskedPhoneNumber(TEST_USER_ID))
                    .thenThrow(new UserNotFoundException(TEST_USER_ID));

            // When & Then
            mockMvc.perform(get("/api/users/detail/{id}/masked-phone", TEST_USER_ID))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    /* Level 4: Mutation Testing (각 메서드 1개 - 필수) */

    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {

        @Test
        @DisplayName("GET /api/users/list - userService.getAllUsers() 호출 검증")
        void should_callGetAllUsers_when_getAllUsersEndpoint() throws Exception {
            // Given
            when(userService.getAllUsers()).thenReturn(Collections.emptyList());

            // When
            mockMvc.perform(get("/api/users/list"));

            // Then
            verify(userService).getAllUsers();
        }

        @Test
        @DisplayName("POST /api/users/create - CreateUserRequest 정확하게 전달되는지 검증")
        void should_passCorrectCreateUserRequest_when_createUserEndpoint() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            User createdUser = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

            String jsonContent = objectMapper.writeValueAsString(request);

            // When
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON));

            // Then - 전달된 인자의 필드값 정밀 검증
            verify(userService).createUser(argThat(req ->
                req.getName().equals(TEST_NAME) &&
                req.getEmail().equals(TEST_EMAIL) &&
                req.getPassword().equals(TEST_PASSWORD)
            ));
        }

        @Test
        @DisplayName("GET /api/users/detail/{id} - userService.getUserById() 호출 검증")
        void should_callGetUserById_when_getUserByIdEndpoint() throws Exception {
            // Given
            User user = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.getUserById(TEST_USER_ID)).thenReturn(user);

            // When
            mockMvc.perform(get("/api/users/detail/{id}", TEST_USER_ID));

            // Then
            verify(userService).getUserById(TEST_USER_ID);
        }

        @Test
        @DisplayName("POST /api/users/create - userService.createUser() 호출 검증")
        void should_callCreateUser_when_createUserEndpoint() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            User createdUser = createTestUser(TEST_USER_ID, TEST_NAME, TEST_EMAIL);
            when(userService.createUser(any(CreateUserRequest.class))).thenReturn(createdUser);

            String jsonContent = objectMapper.writeValueAsString(request);

            // When
            mockMvc.perform(post("/api/users/create")
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON));

            // Then
            verify(userService).createUser(any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("POST /api/users/update/{id} - userService.updateUser() 호출 검증")
        void should_callUpdateUser_when_updateUserEndpoint() throws Exception {
            // Given
            CreateUserRequest request = createTestRequest();
            User updatedUser = createTestUser(TEST_USER_ID, "수정된이름", TEST_EMAIL);
            when(userService.updateUser(eq(TEST_USER_ID), any(CreateUserRequest.class))).thenReturn(updatedUser);

            String jsonContent = objectMapper.writeValueAsString(request);

            // When
            mockMvc.perform(post("/api/users/update/{id}", TEST_USER_ID)
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON));

            // Then
            verify(userService).updateUser(eq(TEST_USER_ID), any(CreateUserRequest.class));
        }

        @Test
        @DisplayName("POST /api/users/delete/{id} - userService.deleteUser() 호출 검증")
        void should_callDeleteUser_when_deleteUserEndpoint() throws Exception {
            // Given
            doNothing().when(userService).deleteUser(TEST_USER_ID);

            // When
            mockMvc.perform(post("/api/users/delete/{id}", TEST_USER_ID));

            // Then
            verify(userService).deleteUser(TEST_USER_ID);
        }

        @Test
        @DisplayName("GET /api/users/detail/{id}/masked-phone - userService.getMaskedPhoneNumber() 호출 검증")
        void should_callGetMaskedPhoneNumber_when_getMaskedPhoneEndpoint() throws Exception {
            // Given
            String maskedPhone = "010-****-5678";
            when(userService.getMaskedPhoneNumber(TEST_USER_ID)).thenReturn(maskedPhone);

            // When
            mockMvc.perform(get("/api/users/detail/{id}/masked-phone", TEST_USER_ID));

            // Then
            verify(userService).getMaskedPhoneNumber(TEST_USER_ID);
        }
    }

    /* Helper Methods */

    private User createTestUser(Long id, String name, String email) {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .password(TEST_PASSWORD)
                .phoneNumber(TEST_PHONE_NUMBER)
                .role("USER")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
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

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| `@WebMvcTest(UserController.class)` | Controller만 테스트, 의존성 자동 구성 |
| `@AutoConfigureMockMvc(addFilters = false)` | Security 필터 비활성화 |
| `@MockBean` | Service 및 Security 빈을 Mock으로 대체 |
| `MockMvc.perform()` | HTTP 요청 시뮬레이션 |
| `@Nested` | 4단계 레벨별 테스트 클래스 분리 |
| `@DisplayName` 한글 | 시나리오를 한글로 명확히 설명 |
| `should_{동작}_when_{조건}` | 테스트 메서드 네이밍 규칙 준수 |

### 3.2. HTTP 상태 코드 검증

| 상황 | 예상 상태 코드 |
|---|---|
| 조회 성공 | `200 OK` |
| 생성 성공 | `201 Created` |
| 삭제 성공 | `204 No Content` |
| 유효성 실패 | `400 Bad Request` |
| 리소스 없음 | `404 Not Found` |
| 중복 충돌 | `409 Conflict` |

### 3.3. NH 도메인 규칙 적용

| 규칙 | 적용 위치 |
|---|---|
| PII 마스킹 검증 | 테스트에서 마스킹 결과 (`010-****-5678`) 검증 |
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"`, `"testPassword"` 사용 |

### 3.4. Controller vs Service 테스트 차이점

| 구분 | Controller 테스트 | Service 테스트 |
|---|---|---|
| 테스트 대상 | HTTP 요청/응답 | 비즈니스 로직 |
| Mock 대상 | Service, Security | Mapper |
| 검증 방식 | MockMvc 응답 검증 | 메서드 반환값 검증 |
| 애너테이션 | `@WebMvcTest` | `@ExtendWith(MockitoExtension.class)` |

> Edge Cases와 Exception Cases의 차이:
> - Edge Cases: 입력값 유효성 검증 (null, 형식, 길이)
> - Exception Cases: 비즈니스 예외 (사용자 미존재, 중복 데이터)