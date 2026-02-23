# NH 네이밍 규칙

> 이 문서는 테스트 코드 생성 시 적용하는 **네이밍 규칙**을 정의합니다.
> 모든 규칙에 좋은 예시와 나쁜 예시를 포함합니다.

---

## 1. 클래스 이름

### 1.1. 소스 클래스

| 계층 | 패턴 | 예시 |
|---|---|---|
| Service | `{도메인}Service` | `UserService`, `AuthService`, `LogTrackerService` |
| Controller | `{도메인}Controller` | `UserController`, `AuthController` |
| Mapper | `{도메인}Mapper` | `UserMapper`, `LogMapper` |
| DTO (요청) | `{동작}{도메인}Request` | `CreateUserRequest`, `UpdateUserRequest` |
| DTO (응답) | `{동작}{도메인}Response` | `CreateUserResponse`, `UserDetailResponse` |
| Utility | `{기능}Util` 또는 `{기능}Helper` | `MaskingUtil`, `DateUtil`, `StringHelper` |
| Exception | `{상황}Exception` | `DuplicateEmailException`, `UserNotFoundException` |

### 1.2. 테스트 클래스

| 패턴 | 예시 |
|---|---|
| `{원본클래스명}Test` | `UserServiceTest`, `UserControllerTest` |
| DB 연동 테스트: `{원본클래스명}IntegrationTest` | `UserMapperIntegrationTest` |

```java
// 좋은 예시
class UserServiceTest { }
class UserControllerTest { }

// 나쁜 예시
class UserServiceTestCase { }    // "TestCase" 접미어 사용 금지
class TestUserService { }        // "Test" 접두어 사용 금지
class UserServiceTests { }       // 복수형 사용 금지
```

---

## 2. 메서드 이름

### 2.1. 소스 메서드

| 패턴 | 예시 |
|---|---|
| `{동사}{객체}` | `getUserById`, `createUser`, `deleteUser` |
| `{동사}{객체}By{조건}` | `findUserByEmail`, `countLogsByDate` |

### 2.2. 테스트 메서드

**패턴**: `should_{동작}_when_{조건}`

```java
// 좋은 예시
void should_createUser_when_validRequest() { }
void should_throwException_when_emailIsDuplicate() { }
void should_returnNull_when_userNotFound() { }
void should_encodePassword_when_creatingUser() { }

// 나쁜 예시
void testCreateUser() { }                    // test 접두어
void createUserTest() { }                    // Test 접미어
void should_create_user_successfully() { }   // when 절 누락
void userCreationWorks() { }                 // 패턴 미준수
```

### 2.3. @DisplayName (한글)

모든 테스트 메서드에 `@DisplayName`을 추가하고, **한글**로 시나리오를 설명합니다.

```java
// 좋은 예시
@DisplayName("유효한 요청으로 사용자 생성 성공")
@DisplayName("중복 이메일이면 DuplicateEmailException 발생")
@DisplayName("ID가 null이면 InvalidUserIdException 발생")
@DisplayName("GET /api/users/{id} 정상 조회 시 200 OK")

// 나쁜 예시
@DisplayName("should create user when valid request")   // 영문
@DisplayName("테스트1")                                  // 의미 없음
@DisplayName("createUser")                               // 메서드명 반복
```

---

## 3. 변수 이름

### 3.1. 테스트 데이터 변수

| 용도 | 패턴 | 예시 |
|---|---|---|
| 요청 객체 | `request`, `{동작}Request` | `request`, `createRequest` |
| 기대 결과 | `expected`, `expected{객체}` | `expected`, `expectedUser` |
| 실제 결과 | `result`, `actual` | `result`, `actual` |
| 테스트 입력 | `test{데이터명}` | `testEmail`, `testPassword` |
| Mock 반환값 | `mock{객체}`, `{설명적이름}` | `mockUser`, `encodedPassword` |

```java
// 좋은 예시
CreateUserRequest request = new CreateUserRequest("테스트사용자", "test@example.com", "testPassword");
User expectedUser = new User("테스트사용자", "test@example.com", "encodedPassword");
User result = userService.createUser(request);

// 나쁜 예시
CreateUserRequest r = new CreateUserRequest(...);    // 축약형
User u = new User(...);                               // 축약형
User res = userService.createUser(r);                  // 축약형
```

### 3.2. Mock 변수

| 패턴 | 예시 |
|---|---|
| `@Mock` 필드: 원본 타입의 camelCase | `userMapper`, `passwordEncoder`, `auditLogService` |
| `@InjectMocks` 필드: 테스트 대상 camelCase | `userService`, `userController` |

```java
// 좋은 예시
@Mock private UserMapper userMapper;
@Mock private PasswordEncoder passwordEncoder;
@InjectMocks private UserService userService;

// 나쁜 예시
@Mock private UserMapper mockUserMapper;    // mock 접두어 불필요 (@Mock이 이미 표시)
@Mock private UserMapper um;                // 축약형
```

### 3.3. 상수

```java
// 좋은 예시
private static final Long VALID_USER_ID = 1L;
private static final String TEST_EMAIL = "test@example.com";

// 나쁜 예시
private static final Long id = 1L;              // 소문자, 설명 부족
private static final String email = "test@example.com";  // 소문자
```

---

## 4. 패키지 이름

### 4.1. 소스 패키지

```
com.nhcard.{프로젝트}.{모듈}
  ├── controller/
  ├── service/
  ├── mapper/
  ├── dto/
  ├── entity/
  └── util/
```

### 4.2. 테스트 패키지

소스 패키지와 **동일한 구조**로 구성합니다.

```
src/test/java/com/nhcard/{프로젝트}/{모듈}/
  ├── controller/
  │   └── UserControllerTest.java
  ├── service/
  │   └── UserServiceTest.java
  ├── mapper/
  │   └── UserMapperTest.java
  └── util/
      └── MaskingUtilTest.java
```
