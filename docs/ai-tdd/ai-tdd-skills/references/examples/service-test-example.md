# 서비스 테스트 예제

> `@Service` 클래스의 단위 테스트 완성 예제입니다.
> 4단계 레벨(Happy/Edge/Exception/Mutation)을 모두 포함합니다.

---

## 1. 소스 클래스

```java
@Service
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, AuditLogService auditLogService) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
    }

    public User createUser(CreateUserRequest request) {
        if (userMapper.findByEmail(request.getEmail()) != null) {
            throw new DuplicateEmailException("이미 존재하는 이메일입니다");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(), encodedPassword);
        userMapper.insert(user);

        auditLogService.log(new AuditLog("CREATE", "User", user.getId()));
        return user;
    }

    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new InvalidUserIdException("유효하지 않은 사용자 ID입니다");
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new UserNotFoundException("사용자를 찾을 수 없습니다: " + id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userMapper.deleteById(id);
        auditLogService.log(new AuditLog("DELETE", "User", user.getId()));
    }
}
```

---

## 2. 테스트 클래스

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private UserService userService;

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("유효한 요청으로 사용자 생성 성공")
    void should_createUser_when_validRequest() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encryptedValue");

        // When
        User result = userService.createUser(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트사용자");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("유효한 ID로 사용자 조회 성공")
    void should_returnUser_when_validId() {
        // Given
        Long userId = 1L;
        User expectedUser = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findById(userId)).thenReturn(expectedUser);

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("전체 사용자 목록 조회 성공")
    void should_returnAllUsers_when_called() {
        // Given
        List<User> expectedUsers = List.of(
            new User("사용자1", "user1@example.com", "enc1"),
            new User("사용자2", "user2@example.com", "enc2")
        );
        when(userMapper.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("사용자1");
    }

    @Test
    @DisplayName("유효한 ID로 사용자 삭제 성공")
    void should_deleteUser_when_validId() {
        // Given
        Long userId = 1L;
        User existingUser = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findById(userId)).thenReturn(existingUser);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userMapper).deleteById(userId);
    }

    // ── Level 2: Edge Case (30%) ──

    @Test
    @DisplayName("사용자 목록이 비어있으면 빈 리스트 반환")
    void should_returnEmptyList_when_noUsersExist() {
        // Given
        when(userMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("ID가 경계값(1)이면 정상 조회")
    void should_returnUser_when_idIsBoundaryValue() {
        // Given
        Long boundaryId = 1L;
        User expectedUser = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findById(boundaryId)).thenReturn(expectedUser);

        // When
        User result = userService.getUserById(boundaryId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트사용자");
    }

    // ── Level 3: Exception (20%) ──

    @Test
    @DisplayName("중복 이메일이면 DuplicateEmailException 발생")
    void should_throwException_when_emailIsDuplicate() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(new User());

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessage("이미 존재하는 이메일입니다");
    }

    @Test
    @DisplayName("ID가 null이면 InvalidUserIdException 발생")
    void should_throwException_when_idIsNull() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(null))
            .isInstanceOf(InvalidUserIdException.class)
            .hasMessage("유효하지 않은 사용자 ID입니다");
    }

    @Test
    @DisplayName("ID가 0 이하이면 InvalidUserIdException 발생")
    void should_throwException_when_idIsZeroOrNegative() {
        // When & Then
        assertThatThrownBy(() -> userService.getUserById(0L))
            .isInstanceOf(InvalidUserIdException.class);

        assertThatThrownBy(() -> userService.getUserById(-1L))
            .isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    @DisplayName("존재하지 않는 ID이면 UserNotFoundException 발생")
    void should_throwException_when_userNotFound() {
        // Given
        when(userMapper.findById(999L)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(999L))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("999");
    }

    // ── Level 4: Mutation Testing (10%) ──

    @Test
    @DisplayName("사용자 생성 시 비밀번호가 Petra 암호화됨")
    void should_encryptPassword_when_creatingUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(null);
        when(passwordEncoder.encode("testPassword")).thenReturn("encryptedValue");

        // When
        userService.createUser(request);

        // Then - 암호화 메서드 호출 검증
        verify(passwordEncoder).encode("testPassword");

        // 평문 비밀번호가 직접 저장되지 않았는지 확인
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getPassword()).isNotEqualTo("testPassword");
        assertThat(captor.getValue().getPassword()).isEqualTo("encryptedValue");
    }

    @Test
    @DisplayName("사용자 생성 시 이메일 중복 체크가 insert보다 먼저 실행됨")
    void should_checkDuplicateBeforeInsert_when_creatingUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedValue");

        // When
        userService.createUser(request);

        // Then - 호출 순서 검증
        InOrder inOrder = inOrder(userMapper);
        inOrder.verify(userMapper).findByEmail(request.getEmail());
        inOrder.verify(userMapper).insert(any(User.class));
    }

    @Test
    @DisplayName("사용자 생성 시 감사로그가 기록됨")
    void should_writeAuditLog_when_creatingUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("encryptedValue");

        // When
        userService.createUser(request);

        // Then - 감사로그 기록 검증
        verify(auditLogService).log(argThat(log ->
            log.getAction().equals("CREATE") &&
            log.getTargetType().equals("User")
        ));
    }

    @Test
    @DisplayName("사용자 삭제 시 감사로그가 기록됨")
    void should_writeAuditLog_when_deletingUser() {
        // Given
        Long userId = 1L;
        User existingUser = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findById(userId)).thenReturn(existingUser);

        // When
        userService.deleteUser(userId);

        // Then - 삭제 + 감사로그 순서 검증
        InOrder inOrder = inOrder(userMapper, auditLogService);
        inOrder.verify(userMapper).deleteById(userId);
        inOrder.verify(auditLogService).log(argThat(log ->
            log.getAction().equals("DELETE")
        ));
    }

    @Test
    @DisplayName("중복 이메일이면 insert가 호출되지 않음")
    void should_notInsert_when_emailIsDuplicate() {
        // Given
        CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
        when(userMapper.findByEmail(request.getEmail())).thenReturn(new User());

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(DuplicateEmailException.class);

        verify(userMapper, never()).insert(any(User.class));
        verify(auditLogService, never()).log(any(AuditLog.class));
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

### 3.2. NH 도메인 규칙 적용

| 규칙 | 적용 위치 |
|---|---|
| Petra 암호화 검증 | `should_encryptPassword_when_creatingUser` - `verify(passwordEncoder).encode()` + `ArgumentCaptor`로 평문 미저장 확인 |
| 감사로그 검증 | `should_writeAuditLog_when_creatingUser`, `should_writeAuditLog_when_deletingUser` |
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"`, `"testPassword"` 사용 |

### 3.3. 4단계 레벨 분포

| 레벨 | 테스트 수 | 비율 | 테스트 메서드 |
|---|---|---|---|
| Level 1: Happy Case | 4개 | 27% | createUser, getUserById, getAllUsers, deleteUser |
| Level 2: Edge Case | 2개 | 13% | emptyList, boundaryValue |
| Level 3: Exception | 4개 | 27% | duplicateEmail, nullId, zeroId, notFound |
| Level 4: Mutation | 5개 | 33% | encryptPassword, checkOrder, auditLog(2), notInsert |

> 이 예제는 NH 도메인 규칙 검증(Level 4)을 강조하여 Mutation 비중이 높습니다.
> 실제 생성 시 소스 코드 복잡도에 따라 비율을 40-30-20-10에 가깝게 조정합니다.
