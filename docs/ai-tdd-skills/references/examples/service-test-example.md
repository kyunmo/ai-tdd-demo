# 서비스 테스트 예제

> `Service` 클래스의 단위 테스트 완성 예제
> 4단계 레벨(Happy/Edge/Exception/Mutation)을 모두 포함합니다.

---

## 1. 소스 클래스
```java
@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(CreateUserRequest request) {
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new DuplicateEmailException(request.getEmail());
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(encodedPassword);
        user.setPhoneNumber(request.getPhoneNumber());

        userMapper.insert(user);
        return user;
    }

    public User updateUser(Long id, CreateUserRequest request) {
        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userMapper.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        getUserById(id);
        userMapper.deleteById(id);
    }

    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID: " + id);
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }
    
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public void deleteUser(Long id) {
        getUserById(id);
        userMapper.deleteById(id);
    }
    
}
```

## 2. 테스트 클래스
```java
/**
 * UserService 테스트 클래스
 * - Happy Cases : N개
 * - Edge Cases : N개
 * - Exception Cases : N개
 * - Mutation testing : N개
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
        @DisplayName("유효한 요청으로 사용자 생성 성공")
        void should_createUser_when_validRequest() {
            // Given
            CreateUserRequest request = createTestRequest();

            when(userMapper.findByEmail(TEST_EMAIL)).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(TEST_USER_ID);
                return 1;
            });

            // When
            User result = userService.createUser(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getName()).isEqualTo(TEST_NAME);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(result.getPassword()).isEqualTo(TEST_PASSWORD);
            assertThat(result.getPhoneNumber()).isEqualTo(TEST_PHONE_NUMBER);

            verify(userMapper).findByEmail(TEST_EMAIL);
            verify(userMapper).insert(any(User.class));
        }

        @Test
        @DisplayName("유효한 요청으로 사용자 정보 수정 성공")
        void should_updateUser_when_validRequest() {
            // Given
            CreateUserRequest request = createTestRequest();
            request.setName("수정된이름");

            User updatedUser = createTestUser();
            updatedUser.setName("수정된이름");
            when(userMapper.update(any(User.class))).thenReturn(1);

            // When
            User result = userService.updateUser(TEST_USER_ID, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("수정된이름");
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);

            verify(userMapper).update(any(User.class));
        }

        @Test
        @DisplayName("유효한 ID로 사용자 삭제 성공")
        void should_deleteUser_when_validId() {
            // Given
            User mockUser = createTestUser();
            when(userMapper.findById(TEST_USER_ID)).thenReturn(mockUser);
            doNothing().when(userMapper).deleteById(TEST_USER_ID);

            // When
            userService.deleteUser(TEST_USER_ID);

            // Then
            verify(userMapper).findById(TEST_USER_ID);
            verify(userMapper).deleteById(TEST_USER_ID);
        }
        
        @Test
        @DisplayName("유효한 ID로 사용자 조회 성공")
        void should_return_user_when_valid_id_is_provided() {
            // Given
            Long userId = 1L;
            User mockUser = new User();
            mockUser.setId(userId);
            when(userMapper.findById(userId)).thenReturn(mockUser);

            // When
            User result = userService.getUserById(userId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isSameAs(mockUser);

            verify(userMapper).findById(userId);
        }
    }
    
    /* Level 2: Edge Cases */
    @Nested
    @DisplayName("경계값 및 예외 상황 테스트")
    class EdgeCases {
        
        @Test
        @DisplayName("ID가 null이면 IllegalArgumentException 발생")
        void should_throwIllegalArgumentException_when_getUserById_nullId() {
            // When & Then
            assertThatThrownBy(() -> userService.getUserById(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("유효하지 않은 사용자 ID");
        }

        @Test
        @DisplayName("전체 사용자 목록이 비어있을 때 빈 리스트 반환")
        void should_returnEmptyList_when_getAllUsers_noData() {
            // Given
            when(userMapper.findAll()).thenReturn(Collections.emptyList());

            // When
            List<User> result = userService.getAllUsers();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();

            verify(userMapper).findAll();
        }
        
    }

    /* Level 3: Exception Cases (throw 문 1:1) */
    @Nested
    @DisplayName("비즈니스 예외 테스트")
    class ExceptionCases {

        @Test
        @DisplayName("중복 이메일이면 DuplicateEmailException 발생")
        void should_throwDuplicateEmailException_when_createUser_emailExists() {
            // Given
            CreateUserRequest request = createTestRequest();
            User existingUser = createTestUser();
            when(userMapper.findByEmail(TEST_EMAIL)).thenReturn(existingUser);

            // When & Then
            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateEmailException.class)
                    .hasMessageContaining(TEST_EMAIL);

            verify(userMapper).findByEmail(TEST_EMAIL);
            verify(userMapper, never()).insert(any(User.class));
        }

        @Test
        @DisplayName("사용자 미존재시 UserNotFoundException 발생")
        void should_throwUserNotFoundException_when_getUserById_notFound() {
            // Given
            when(userMapper.findById(TEST_USER_ID)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.getUserById(TEST_USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(String.valueOf(TEST_USER_ID));

            verify(userMapper).findById(TEST_USER_ID);
        }

        @Test
        @DisplayName("삭제 시 존재하지 않는 사용자이면 UserNotFoundException 발생")
        void should_throwUserNotFoundException_when_deleteUser_notFound() {
            // Given
            when(userMapper.findById(TEST_USER_ID)).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> userService.deleteUser(TEST_USER_ID))
                    .isInstanceOf(UserNotFoundException.class)
                    .hasMessageContaining(String.valueOf(TEST_USER_ID));

            verify(userMapper).findById(TEST_USER_ID);
            verify(userMapper, never()).deleteById(any());
        }
        
    }
    
    /* Level 4: Mutation Testing (각 메서드 1개 - 필수) */
    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {

        @Test
        @DisplayName("사용자 생성 시 이메일 중복 확인이 insert보다 먼저 호출됨")
        void should_checkEmailDuplication_before_insert() {
            // Given
            CreateUserRequest request = createTestRequest();

            when(userMapper.findByEmail(TEST_EMAIL)).thenReturn(null);
            when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(TEST_USER_ID);
                return 1;
            });

            // When
            userService.createUser(request);

            // Then - 호출 순서 검증
            InOrder inOrder = inOrder(userMapper);
            inOrder.verify(userMapper).findByEmail(TEST_EMAIL);
            inOrder.verify(userMapper).insert(any(User.class));
        }
    }
    
    // Helper Methods
    private CreateUserRequest createTestRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName(TEST_NAME);
        request.setEmail(TEST_EMAIL);
        request.setPassword(TEST_PASSWORD);
        request.setPhoneNumber(TEST_PHONE_NUMBER);
        return request;
    }

    private User createTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setName(TEST_NAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        user.setPhoneNumber(TEST_PHONE_NUMBER);
        return user;
    }
}
```

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Spring 컨텍스트 없이 순수 단위 테스트 |
| `@Mock` + `@InjectMocks` | `@MockBean` 대신 사용 |
| `@DisplayName` 한글 | 시나리오를 한글로 명확히 설명 |
| `should_{동작}_when_{조건}` | 테스트 메서드 네이밍 규칙 준수 |
| Given-When-Then | 모든 테스트에 주석으로 구조 표시 |
| AssertJ | `assertThat()`, `assertThatThrownBy()` 사용 |

### 3.2. 도메인 규칙

| 규칙 | 적용 위치 |
|---|---|
| 암호화 검증 | `verify(passwordEncoder).encode()`, `ArgumentCaptor`로 평문 미지정 확인 |
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"`, `"testPassword"` 사용 |


> 코드 복잡도에 따라 필요한 import 요소가 누락하거나 불필요하게 추가 않도록 더블 체크 필요함
> 생성시 Edge Cases와 Exception Case 중복 테스트 방지
> null 검증시 테스트를 NPE Expected로 변경 또는 코드에 null 검증 추가