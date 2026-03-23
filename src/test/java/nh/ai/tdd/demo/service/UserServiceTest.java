package nh.ai.tdd.demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nh.ai.tdd.demo.domain.User;
import nh.ai.tdd.demo.dto.CreateUserRequest;
import nh.ai.tdd.demo.exception.DuplicateEmailException;
import nh.ai.tdd.demo.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    private RestTemplate restTemplate;
    private MockRestServiceServer mockServer;
    private UserService userService;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        restTemplate = new RestTemplate();
        mockServer = MockRestServiceServer.createServer(restTemplate);
        userService = new UserService(userMapper, restTemplate);
    }

    @Nested
    @DisplayName("createUser 메서드")
    class CreateUser {

        @Test
        @DisplayName("정상적인 요청 시 사용자를 생성하고 외부 ID를 포함해야 한다")
        void shouldCreateUserAndIncludeExternalId_whenRequestIsOk() throws JsonProcessingException {
            // Given
            CreateUserRequest request = new CreateUserRequest("testuser", "test@test.com", "password", "01012345678");
            when(userMapper.findByEmail("test@test.com")).thenReturn(null);

            // 외부 API 응답 Mocking
            String externalId = "ext-12345";
            Map<String, String> mockResponse = Collections.singletonMap("externalId", externalId);
            mockServer.expect(requestTo("http://localhost:8080/mock-api/external-system/user-info"))
                    .andExpect(method(HttpMethod.GET))
                    .andRespond(withSuccess(objectMapper.writeValueAsString(mockResponse), MediaType.APPLICATION_JSON));


            // When
            userService.createUser(request);

            // Then
            mockServer.verify();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getName()).isEqualTo("testuser");
            assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
            assertThat(savedUser.getExternalId()).isEqualTo(externalId);
        }

        @Test
        @DisplayName("이메일이 중복되면 DuplicateEmailException을 발생시켜야 한다")
        void shouldThrowDuplicateEmailException_whenEmailIsDuplicated() {
            // Given
            CreateUserRequest request = new CreateUserRequest("testuser", "test@test.com", "password", "01012345678");
            when(userMapper.findByEmail("test@test.com")).thenReturn(new User());

            // When / Then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining("이미 존재하는 이메일입니다");

            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("외부 API 호출이 실패해도 사용자는 생성되어야 하며 externalId는 null이어야 한다")
        void shouldCreateUserWithNullExternalId_whenExternalApiCallFails() {
            // Given
            CreateUserRequest request = new CreateUserRequest("testuser", "test@test.com", "password", "01012345678");
            when(userMapper.findByEmail("test@test.com")).thenReturn(null);

            // 외부 API 응답 실패 Mocking
            mockServer.expect(requestTo("http://localhost:8080/mock-api/external-system/user-info"))
                    .andRespond(org.springframework.test.web.client.response.MockRestResponseCreators.withServerError());


            // When
            userService.createUser(request);

            // Then
            mockServer.verify();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userMapper).insert(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getName()).isEqualTo("testuser");
            assertThat(savedUser.getEmail()).isEqualTo("test@test.com");
            assertThat(savedUser.getExternalId()).isNull();
        }
    }
}
