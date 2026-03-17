# 맵퍼 테스트 예제

> `Mapper` 인터페이스의 Mock 기반 단위 테스트 완성 예제
> 4단계 레벨(Happy/Edge/Exception/Mutation)을 모두 포함합니다.

---

## 1. 소스 클래스
```java
@Mapper
public interface UserMapper {

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int count();
}
```

## 2. 테스트 클래스
```java
package com.nhcard.al.demo.mapper;

import com.nhcard.al.demo.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

/**
 * UserMapper 테스트 클래스 (Mock 기반)
 * - Happy Cases : 7개
 * - Edge Cases : 4개
 * - Exception Cases : 2개
 * - Mutation Testing : 4개
 */
@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private UserMapper userMapper;

    // 테스트 데이터
    private static final Long TEST_USER_ID = 1L;
    private static final String TEST_NAME = "테스트사용자";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "encodedPassword";

    /* -------------------------------------------------
     * Level 1: Happy Cases (각 메서드 1개)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("정상 케이스 테스트")
    class HappyCases {

        @Test
        @DisplayName("정상 ID로 사용자 단건 조회 성공")
        void should_returnUser_when_findById() {
            // Given
            User mockUser = createTestUser();
            when(userMapper.findById(TEST_USER_ID)).thenReturn(mockUser);

            // When
            User result = userMapper.findById(TEST_USER_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(TEST_USER_ID);
            assertThat(result.getName()).isEqualTo(TEST_NAME);
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("이메일로 사용자 조회 성공")
        void should_returnUser_when_findByEmail() {
            // Given
            User mockUser = createTestUser();
            when(userMapper.findByEmail(TEST_EMAIL)).thenReturn(mockUser);

            // When
            User result = userMapper.findByEmail(TEST_EMAIL);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        }

        @Test
        @DisplayName("전체 사용자 목록 조회 성공")
        void should_returnUserList_when_findAll() {
            // Given
            User user1 = createTestUser();
            User user2 = createTestUser();
            user2.setId(2L);
            user2.setName("테스트사용자2");
            when(userMapper.findAll()).thenReturn(Arrays.asList(user1, user2));

            // When
            List<User> result = userMapper.findAll();

            // Then
            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(TEST_USER_ID);
        }

        @Test
        @DisplayName("사용자 등록 성공")
        void should_returnOne_when_insert() {
            // Given
            User user = createTestUser();
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // When
            int result = userMapper.insert(user);

            // Then
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("사용자 정보 수정 성공")
        void should_returnOne_when_update() {
            // Given
            User user = createTestUser();
            when(userMapper.update(any(User.class))).thenReturn(1);

            // When
            int result = userMapper.update(user);

            // Then
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("사용자 삭제 성공")
        void should_returnOne_when_deleteById() {
            // Given
            when(userMapper.deleteById(TEST_USER_ID)).thenReturn(1);

            // When
            int result = userMapper.deleteById(TEST_USER_ID);

            // Then
            assertThat(result).isEqualTo(1);
        }

        @Test
        @DisplayName("사용자 수 조회 성공")
        void should_returnCount_when_count() {
            // Given
            when(userMapper.count()).thenReturn(10);

            // When
            int result = userMapper.count();

            // Then
            assertThat(result).isEqualTo(10);
        }
    }

    /* -------------------------------------------------
     * Level 2: Edge Cases
     * ------------------------------------------------- */

    @Nested
    @DisplayName("경계값 테스트")
    class EdgeCases {

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
        void should_returnNull_when_findById_notExists() {
            // Given
            when(userMapper.findById(999L)).thenReturn(null);

            // When
            User result = userMapper.findById(999L);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 조회 시 null 반환")
        void should_returnNull_when_findByEmail_notExists() {
            // Given
            when(userMapper.findByEmail("notexist@example.com")).thenReturn(null);

            // When
            User result = userMapper.findByEmail("notexist@example.com");

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("사용자가 없으면 빈 리스트 반환")
        void should_returnEmptyList_when_findAll_noData() {
            // Given
            when(userMapper.findAll()).thenReturn(Collections.emptyList());

            // When
            List<User> result = userMapper.findAll();

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("삭제 대상이 없으면 0 반환")
        void should_returnZero_when_deleteById_notExists() {
            // Given
            when(userMapper.deleteById(999L)).thenReturn(0);

            // When
            int result = userMapper.deleteById(999L);

            // Then
            assertThat(result).isZero();
        }
    }

    /* -------------------------------------------------
     * Level 3: Exception Cases
     * ------------------------------------------------- */

    @Nested
    @DisplayName("예외 테스트")
    class ExceptionCases {

        @Test
        @DisplayName("중복 이메일 insert 시 DataAccessException 발생")
        void should_throwException_when_insert_duplicateEmail() {
            // Given
            User user = createTestUser();
            when(userMapper.insert(any(User.class)))
                    .thenThrow(new org.springframework.dao.DuplicateKeyException("Duplicate entry"));

            // When & Then
            assertThatThrownBy(() -> userMapper.insert(user))
                    .isInstanceOf(org.springframework.dao.DuplicateKeyException.class);
        }

        @Test
        @DisplayName("DB 오류 시 DataAccessException 발생")
        void should_throwException_when_findAll_dbError() {
            // Given
            when(userMapper.findAll())
                    .thenThrow(new org.springframework.dao.DataAccessResourceFailureException("DB connection failed"));

            // When & Then
            assertThatThrownBy(() -> userMapper.findAll())
                    .isInstanceOf(org.springframework.dao.DataAccessResourceFailureException.class);
        }
    }

    /* -------------------------------------------------
     * Level 4: Mutation Testing (각 메서드 1개 - 필수)
     * ------------------------------------------------- */

    @Nested
    @DisplayName("변이 테스트")
    class MutationTesting {

        @Test
        @DisplayName("findById 호출 시 전달된 ID 값 정밀 검증")
        void should_passCorrectId_when_findById() {
            // Given
            when(userMapper.findById(TEST_USER_ID)).thenReturn(createTestUser());

            // When
            userMapper.findById(TEST_USER_ID);

            // Then
            verify(userMapper).findById(TEST_USER_ID);
            verify(userMapper, never()).findById(2L);
        }

        @Test
        @DisplayName("insert 호출 시 전달된 User 객체 필드값 검증")
        void should_passCorrectUser_when_insert() {
            // Given
            User user = createTestUser();
            when(userMapper.insert(any(User.class))).thenReturn(1);

            // When
            userMapper.insert(user);

            // Then
            verify(userMapper).insert(argThat(u ->
                u.getName().equals(TEST_NAME) &&
                u.getEmail().equals(TEST_EMAIL)
            ));
        }

        @Test
        @DisplayName("deleteById가 정확히 1번 호출됨")
        void should_callDeleteByIdOnce_when_deleteById() {
            // Given
            when(userMapper.deleteById(TEST_USER_ID)).thenReturn(1);

            // When
            userMapper.deleteById(TEST_USER_ID);

            // Then
            verify(userMapper, times(1)).deleteById(TEST_USER_ID);
        }

        @Test
        @DisplayName("update 호출 시 전달된 User 객체 필드값 검증")
        void should_passCorrectUser_when_update() {
            // Given
            User user = createTestUser();
            user.setName("수정된이름");
            when(userMapper.update(any(User.class))).thenReturn(1);

            // When
            userMapper.update(user);

            // Then
            verify(userMapper).update(argThat(u ->
                u.getName().equals("수정된이름")
            ));
        }
    }

    // Helper Methods
    private User createTestUser() {
        User user = new User();
        user.setId(TEST_USER_ID);
        user.setName(TEST_NAME);
        user.setEmail(TEST_EMAIL);
        user.setPassword(TEST_PASSWORD);
        return user;
    }
}
```

---

## 3. 예제 해설

### 3.1. 적용된 규칙

| 규칙 | 적용 내용 |
|---|---|
| `@ExtendWith(MockitoExtension.class)` | Mock 기반 단위 테스트 |
| `@Mock` | 맵퍼 인터페이스를 Mock으로 선언 |
| `@DisplayName` 한글 | 시나리오를 한글로 명확히 설명 |
| `should_{동작}_when_{조건}` | 테스트 메서드 네이밍 규칙 준수 |
| Given-When-Then | 모든 테스트에 주석으로 구조 표시 |
| AssertJ | `assertThat()`, `assertThatThrownBy()` 사용 |

### 3.2. CRUD 메서드별 검증 패턴

| CRUD | Mock 패턴 | 검증 포인트 |
|---|---|---|
| SELECT (단건) | `when(mapper.findById(id)).thenReturn(entity)` | 반환값 필드 검증 |
| SELECT (목록) | `when(mapper.findAll()).thenReturn(List)` | 리스트 크기, 요소 검증 |
| INSERT | `when(mapper.insert(entity)).thenReturn(1)` | 반환값(영향 행 수) 검증 |
| UPDATE | `when(mapper.update(entity)).thenReturn(1)` | 반환값 검증, 인자 검증 |
| DELETE | `when(mapper.deleteById(id)).thenReturn(1)` | 반환값 검증 |
| COUNT | `when(mapper.count()).thenReturn(N)` | 정확한 카운트 검증 |

### 3.3. 테스트 데이터 규칙

| 규칙 | 적용 위치 |
|---|---|
| 안전한 테스트 데이터 | `"테스트사용자"`, `"test@example.com"`, `"encodedPassword"` 사용 |
| 실제 개인정보 사용 금지 | 더미 데이터만 사용 |
