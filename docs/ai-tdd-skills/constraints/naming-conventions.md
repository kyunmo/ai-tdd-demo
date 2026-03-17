# NH 이름 규칙

> 이 문서는 테스트 코드 생성 시 적용하는 **네이밍 규칙**을 정의합니다.
> 모든 규칙에 좋은 예시와 나쁜 예시를 포함합니다.

---

## 1. 클래스 이름

### 1.1. 소스 클래스

| 계층 | 패턴 | 예시 |
|---|---|---|
| Service | `{도메인}Service` | `UserService`, `AuthService`, `UserAuditLogService` |
| Controller | `{도메인}Controller` | `UserController`, `AuthController` |
| Mapper | `{도메인}Mapper` | `UserMapper`, `UserAuditMapper` |
| DTO (요청) | `{도메인}{동작}Request`, `{동작}{도메인}Request` | `UserRegistrationRequest`, `UserUpdateRequest` |
| DTO (응답) | `{도메인}{동작}Response`, `{동작}{도메인}Response` | `UserRegistrationResponse`, `UserDetailResponse` |
| Utility | `{기능}Util` 또는 `{기능}Helper` | `StringUtil`, `DateTimeUtil` |
| Exception | `{상황}Exception` | `BusinessException`, `AuthenticationException`, `TokenException` |

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
class UserServiceTestCase { }   // "TestCase" 접미어 사용 금지
class TestUserService { }       // "Test" 접두어 사용 금지
class UserServiceTests { }      // 복수형 사용 금지
```

---

## 2. 메서드 이름

### 2.1. 소스 메서드

| 패턴 | 예시 |
|---|---|
| `{동사}{객체}` | `getUserById`, `registerUser`, `deleteUser` |
| `{동사}{객체}By{조건}` | `findById`, `countByUserId` |

### 2.2. 테스트 메서드

**패턴**: `should_{동작}_when_{조건}`

```java
// 좋은 예시
void should_registerUser_when_validRequest() { }
void should_throwException_when_userIdIsDuplicate() { }
void should_returnNull_when_userNotFound() { }
void should_encodePassword_when_registerUser() { }

// 나쁜 예시
void testRegisterUser() { }
void registerUserTest() { }
void should_register_user_successfully() { }
void userCreationWorks() { }
```

### 2.3. @DisplayName (한글)

모든 테스트 메서드에 `@DisplayName`을 추가하고, **한글**로 시나리오를 설명합니다.

```java
// 좋은 예시
@DisplayName("유효한 요청으로 사용자 생성 성공")
@DisplayName("중복 아이디면 BusinessException 발생")
@DisplayName("ID가 null이면 InvalidUserIdException 발생")
@DisplayName("GET /api/users/{id} 정상 조회 시 200 OK")

// 나쁜 예시
@DisplayName("should create user when valid request")   // 영문
@DisplayName("테스트1")                                  // 의미 없음
@DisplayName("registerUser")                            // 메서드명 반복
```

---

## 3. 변수 이름

### 3.1. 테스트 데이터 변수

| 용도 | 패턴 | 예시 |
|---|---|---|
| 요청 객체 | `request`, `{동작}request` | `request`, `createRequest` |
| 기대 결과 | `expected`, `expected{객체}` | `expected`, `expectedUser` |
| 실제 결과 | `result` | `result` | 
| 테스트 입력 | `test{데이터명}` | `testEmail`, `testPassword` |
| Mock 반환값 | `mock{객체}`, `{설명적이름}` | `mockUser`, `encodedPassword` |

```java
// 좋은 예시
UserRegistrationRequest request = new UserRegistrationRequest("테스트사용자", "testId", "testPassword");
User expectedUser = new User("테스트사용자", "testId", "encodedPassword");
User result = userService.registerUser(request);

// 나쁜 예시
UserRegistrationRequest r = new UserRegistrationRequest(...);   // 축약형
User u = new User(...);                                         // 축약형
User res = userService.registerUser(r);                         // 축약형
```

### 3.2. Mock 변수

| 패턴 | 예시 |
|---|---|
| `@Mock` 필드: 원본 타입의 camelCase | `userMapper`, `passwordEncoder`, `userAuditLogService` |
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
private static final Long VALID_ID = 1L;
private static final String TEST_EMAIL = "test@example.com";

// 나쁜 예시
private static final Long id = 1L;                      // 소문자, 설명 부족
private static final String email = "test@example.com"; // 소문자
```

---

## 4. 패키지 이름

### 4.1. 소스 패키지

```
com.nhcard.{프로젝트}.{모듈}
├─ controller/
├─ service/
├─ mapper/
├─ dto/
├─ entity/
└─ util/
```

### 4.2. 테스트 패키지

소스 패키지와 **동일한 구조**로 구성합니다.

```
src/test/java/com/nhcard/{프로젝트}/{모듈}/
├─ controller/
│  └─ UserControllerTest.java
├─ service/
│  └─ UserServiceTest.java
├─ mapper/
│  └─ UserMapperTest.java
└─ util/
   └─ DateTimeUtilTest.java
```
