# 맵퍼 테스트 예제

> `@Mapper` 인터페이스의 테스트 예제입니다.
> **Mock 기반 단위 테스트** (서비스에서 간접 검증)와 **DB 연동 통합 테스트** (`@MybatisTest`) 2가지 방식을 모두 포함합니다.

---

## 1. 소스 클래스

```java
@Mapper
public interface UserMapper {

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    List<User> findByStatus(String status);

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int countByStatus(String status);
}
```

---

## 2. Mock 기반 테스트 (단위 테스트)

> 서비스 테스트에서 Mapper를 `@Mock`으로 처리하여 간접 검증하는 방식입니다.
> Mapper 자체보다 **서비스의 Mapper 호출이 올바른지** 검증합니다.

```java
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private UserMapper userMapper;

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("findById 호출 시 사용자 반환")
    void should_returnUser_when_findById() {
        // Given
        User expectedUser = new User(1L, "테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findById(1L)).thenReturn(expectedUser);

        // When
        User result = userMapper.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("테스트사용자");
    }

    @Test
    @DisplayName("findByEmail 호출 시 사용자 반환")
    void should_returnUser_when_findByEmail() {
        // Given
        User expectedUser = new User(1L, "테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.findByEmail("test@example.com")).thenReturn(expectedUser);

        // When
        User result = userMapper.findByEmail("test@example.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("findAll 호출 시 사용자 목록 반환")
    void should_returnUserList_when_findAll() {
        // Given
        List<User> expectedUsers = List.of(
            new User(1L, "사용자1", "user1@example.com", "enc1"),
            new User(2L, "사용자2", "user2@example.com", "enc2")
        );
        when(userMapper.findAll()).thenReturn(expectedUsers);

        // When
        List<User> result = userMapper.findAll();

        // Then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("insert 호출 시 1 반환 (성공)")
    void should_return1_when_insert() {
        // Given
        User user = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        int result = userMapper.insert(user);

        // Then
        assertThat(result).isEqualTo(1);
    }

    // ── Level 2: Edge Case (30%) ──

    @Test
    @DisplayName("findById 결과가 없으면 null 반환")
    void should_returnNull_when_userNotFound() {
        // Given
        when(userMapper.findById(999L)).thenReturn(null);

        // When
        User result = userMapper.findById(999L);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("findAll 결과가 없으면 빈 리스트 반환")
    void should_returnEmptyList_when_noUsers() {
        // Given
        when(userMapper.findAll()).thenReturn(Collections.emptyList());

        // When
        List<User> result = userMapper.findAll();

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByStatus 조건에 맞는 사용자만 반환")
    void should_returnFilteredUsers_when_findByStatus() {
        // Given
        List<User> activeUsers = List.of(
            new User(1L, "활성사용자", "active@example.com", "enc1")
        );
        when(userMapper.findByStatus("ACTIVE")).thenReturn(activeUsers);

        // When
        List<User> result = userMapper.findByStatus("ACTIVE");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("활성사용자");
    }

    // ── Level 3: Exception (20%) ──

    @Test
    @DisplayName("insert 시 DB 오류 발생하면 DataAccessException")
    void should_throwException_when_insertFails() {
        // Given
        User user = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.insert(any(User.class)))
            .thenThrow(new DataAccessException("Duplicate entry") {});

        // When & Then
        assertThatThrownBy(() -> userMapper.insert(user))
            .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("update 대상이 없으면 0 반환")
    void should_return0_when_updateTargetNotExists() {
        // Given
        User user = new User(999L, "테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.update(user)).thenReturn(0);

        // When
        int result = userMapper.update(user);

        // Then
        assertThat(result).isEqualTo(0);
    }

    // ── Level 4: Mutation Testing (10%) ──

    @Test
    @DisplayName("insert 호출 시 전달된 User 객체의 필드값이 정확함")
    void should_passCorrectUser_when_insert() {
        // Given
        User user = new User("테스트사용자", "test@example.com", "encryptedValue");
        when(userMapper.insert(any(User.class))).thenReturn(1);

        // When
        userMapper.insert(user);

        // Then - ArgumentCaptor로 전달된 인자 정밀 검증
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("테스트사용자");
        assertThat(captor.getValue().getEmail()).isEqualTo("test@example.com");
        assertThat(captor.getValue().getPassword()).isEqualTo("encryptedValue");
    }

    @Test
    @DisplayName("deleteById 호출 시 정확한 ID로 호출됨")
    void should_callDeleteWithExactId_when_deleteById() {
        // Given
        when(userMapper.deleteById(1L)).thenReturn(1);

        // When
        userMapper.deleteById(1L);

        // Then
        verify(userMapper).deleteById(eq(1L));
        verify(userMapper, times(1)).deleteById(anyLong());
    }
}
```

---

## 3. DB 연동 테스트 (통합 테스트)

> `@MybatisTest`를 사용하여 **실제 SQL 매핑이 올바른지** 검증하는 방식입니다.
> H2 인메모리 DB를 사용하며, `@Transactional`로 자동 롤백됩니다.

### 3.1. 테스트 데이터 SQL

```sql
-- src/test/resources/sql/user-test-data.sql
INSERT INTO users (id, name, email, password, status) VALUES (1, '테스트사용자1', 'user1@example.com', 'enc1', 'ACTIVE');
INSERT INTO users (id, name, email, password, status) VALUES (2, '테스트사용자2', 'user2@example.com', 'enc2', 'ACTIVE');
INSERT INTO users (id, name, email, password, status) VALUES (3, '비활성사용자', 'user3@example.com', 'enc3', 'INACTIVE');
```

### 3.2. 통합 테스트 클래스

```java
import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

@MybatisTest
@Transactional
@Sql("/sql/user-test-data.sql")
class UserMapperIntegrationTest {

    @Autowired
    private UserMapper userMapper;

    // ── Level 1: Happy Case ──

    @Test
    @DisplayName("findById로 사용자 조회 성공")
    void should_returnUser_when_findById() {
        // When
        User result = userMapper.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("테스트사용자1");
        assertThat(result.getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    @DisplayName("findAll로 전체 사용자 조회 성공")
    void should_returnAllUsers_when_findAll() {
        // When
        List<User> result = userMapper.findAll();

        // Then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("insert로 사용자 추가 성공")
    void should_insertUser_when_validUser() {
        // Given
        User newUser = new User("신규사용자", "new@example.com", "encryptedValue");

        // When
        int result = userMapper.insert(newUser);

        // Then
        assertThat(result).isEqualTo(1);
        User inserted = userMapper.findByEmail("new@example.com");
        assertThat(inserted).isNotNull();
        assertThat(inserted.getName()).isEqualTo("신규사용자");
    }

    // ── Level 2: Edge Case ──

    @Test
    @DisplayName("findByStatus로 상태별 필터링 조회")
    void should_returnFilteredUsers_when_findByStatus() {
        // When
        List<User> activeUsers = userMapper.findByStatus("ACTIVE");
        List<User> inactiveUsers = userMapper.findByStatus("INACTIVE");

        // Then
        assertThat(activeUsers).hasSize(2);
        assertThat(inactiveUsers).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 ID 조회 시 null 반환")
    void should_returnNull_when_idNotExists() {
        // When
        User result = userMapper.findById(999L);

        // Then
        assertThat(result).isNull();
    }

    // ── Level 3: Exception ──

    @Test
    @DisplayName("deleteById 후 재조회 시 null")
    void should_returnNull_when_afterDelete() {
        // When
        int deleteResult = userMapper.deleteById(1L);
        User result = userMapper.findById(1L);

        // Then
        assertThat(deleteResult).isEqualTo(1);
        assertThat(result).isNull();
    }
}
```

---

## 4. 예제 해설

### 4.1. 테스트 방식 선택 기준

| 기준 | Mock 기반 (단위 테스트) | DB 연동 (`@MybatisTest`) |
|---|---|---|
| 목적 | Mapper 호출 패턴 검증 | SQL 매핑 정확성 검증 |
| 속도 | 빠름 (Spring 없음) | 느림 (DB 초기화) |
| 어노테이션 | `@ExtendWith(MockitoExtension.class)` | `@MybatisTest` |
| 클래스명 | `{Mapper}Test` | `{Mapper}IntegrationTest` |
| 사용 시점 | 서비스에서 Mapper 호출 검증 | SQL이 복잡하거나 조인이 있을 때 |

### 4.2. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| `@Transactional` | DB 테스트에서 자동 롤백 보장 |
| `@Sql` | 테스트 데이터 SQL 파일로 초기화 |
| `ArgumentCaptor` | Mock 기반에서 전달된 인자 정밀 검증 |
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"` (실제 데이터 금지) |
