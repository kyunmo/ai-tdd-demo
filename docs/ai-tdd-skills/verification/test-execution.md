# 테스트 실행 검증

> 컴파일을 통과한 테스트 코드가 **모두 성공적으로 실행**되는지 검증합니다.
> 컴파일 체크(`compile-check.md`) 통과 후 이 단계를 실행합니다.
 
---

## 1. 검증 명령어

### 1.1. 전체 테스트 실행

```bash
./gradlew test
```

### 1.2. 특정 테스트 클래스 실행

```bash
./gradlew test --tests "com.nhcard.al.demo.service.UserServiceTest"
```

### 1.3. 특정 테스트 메서드 실행

```bash
./gradlew test --tests "com.nhcard.al.demo.service.UserServiceTest.should_registerUser_when_validRequest"
```

### 1.4. 실패한 테스트만 재실행

```bash
./gradlew test --rerun-tasks
```

### 1.5. 상세 로그 출력

```bash
./gradlew test --info
```

---

## 2. 합격/불합격 기준

| 기준 | 합격 | 불합격 |
|---|---|---|
| 실행 결과 | `BUILD_SUCCESSFUL` | `BUILD_FAILED` |
| 실패 테스트 수 | 0개 | 1개 이상 |
| 스킵 테스트 수 | 0개 | 1개 이상 (`@Disabled` 금지)
| 실행 시간 | 단위테스트: 클래스당 5초 이내 | 

---

## 3. 테스트 결과 보고서 확인

### 3.1. 보고서 위치

```
build/reports/tests/test/index.html
```

### 3.2. 보고서에서 확인할 항목

| 항목 | 확인 내용 |
|---|---|
| Tests summary | 전체 테스트 수, 성공/실패/스킵 수 |
| Failures 탭 | 실패한 테스트 목록 + 오류 메시지 |
| Standard output | System.out 출력 (있으면 제거 필요) |
| Duration | 테스트 실행 시간 (비정상 지연 확인) |

--- 

## 4. 자주 발생하는 실행 오류 및 해결

### 4.1. MockitoException - Mock 초기화 실패

```
org.mockito.exceptions.base.MockitoException;
Cannot instantiate @InjectMocks field named 'userService'
```

**원인**: `@ExtendWith(MockitoExtension.class)` 누락 또는 생성자 파라미터 불일치

**해결**:
```java
// 1. 클래스 어노테이션 확인
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    
    // 2. 소스 클래스의 생성자 파라미터와 @Mock 필드가 일치하는지 확인
    @Mock
    private UserMapper userMapper;              // 생성자 파라미터와 동일한 타입
    
    @Mock
    private PasswordEncoder passwordEncoder;    // 생성자 파라미터와 동일한 타입
    
    @InjectMocks
    private UserService userService;            // 위 Mock이 주입됨
}
```

### 4.2. UnnecessaryStubbingException - 불필요한 Mock 설정

```
org.mockito.exceptions.misusing.UnnecessaryStubbingException:
Unnecessary stubbings detected.
```

**원인**: `when().thenReturn()`으로 설정했으나 실제 테스트에서 호출되지 않음

**해결**:
```java
// 나쁜 예시 - 실제 호출되지 않는 stubbing
when(userMapper.findByEmail(anyString())).thenReturn(null);     // 미사용
when(passwordEncoder.encode(anyString())).thenReturn("enc");    // 미사용
userService.getUserById(anyString());    // 위 두 Mock을 호출하지 않는 메서드

// 좋은 예시 - 테스트 대상 메서드가 실제 호출하는 Mock만 설정
when(userMapper.findById(anyString())).thenReturn(user);    // getUserById가 호출하는 메서드
userService.getUserById(anyString());
```

### 4.3. NullPointerException - Mock 미설정

```
java.lang.NullPointerException
    at com.nhcard.al.demo.service.UserService.getUserById(UserService.java:285)
```

**원인**: 소스 메서드 내부에서 호출하는 의존성의 Mock 반환값 미설정

**해결**:
```java
// 나쁜 예시 - Mock 반환값 미설정
// when(userMapper.findById(anyString())).thenReturn(user); // 누락
User result = userService.getUserById(anyString()); // NPE 발생

// 좋은 예시 - 소스 코드 흐름을 따라 필요한 Mock 모두 설정
when(userMapper.findById(anyString())).thenReturn(user); // 소스 코드에서 호출하는 메서드
User result = userService.getUserById(anyString());
```

### 4.4. AssertionError - 기대값 불일치

```
org.opentest4j.AssertionFailedError:
expected: "테스트사용자"
 but was: "encodeUser"
```

**원인**: Mock 반환값과 기대 결과의 불일치

**해결**:
```java
// 소스 코드의 실제 동작을 확인하고 기대값을 맞춤
// 예: 소스 코드가 "name"을 그대로 반환하는지, 가공하는지 확인

// 1. Mock 설정값과 소스 코드 흐름 추적
when(userMapper.findById(anyString())).thenReturn(
        new User(anyString(), "테스트사용자", "test@example.com");
);

// 2. 소스 코드가 반환하는 실제 값으로 기대값 설정
User result = userService.getUserById(anyString());
assertThat(result.getName()).isEqualTo("테스트사용자"); // Mock 데이터와 일치
```

### 4.5. WebMvcTest 오류 - Bean 누락

```
org.springframework.beans.factory.NoSuchBeanDefinitionException:
No qualifying bean of type 'UserService'
```

**원인**: `@WebMvcTest`에서 Controller가 의존하는 Service를 `@MockBean`으로 선언하지 않음

**해결**:
```java
@WebMvcTest(UserController.class)
class UserControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean                           // @Mock이 아닌 @MockBean 사용
    private UserService userService;    // Controller가 의존하는 서비스
}
```

### 4.6. InvalidUseOfMatchersException - Matcher 혼용

```
org.mockito.exceptions.misusing.InvalidUseOfMatchersException:
Invalid use of argument matchers! 2 matchers expected, 1 recorded.
```

**원인**: 하나의 when 호출에서 구체값과 Matcher를 혼용

**해결**:
```java
// 나쁜 예시 - 구체값과 Matcher 혼용
when(userMapper.findByNameAndEmail("테스트", anyString())).thenReturn(user);

// 좋은 예시 - 모두 Matcher 사용
when(userMapper.findByNameAndEmail(eq("테스트"), anyString())).thenReturn(user);

// 좋은 예시 - 모두 구체값 사용
when(userMapper.findByNameAndEmail("테스트", "test@example.com")).thenReturn(user);
```

---

## 5. 불합격 시 에이전트 조치

테스트 실행 실패 시 다음 순서로 해결합니다.

| 순서 | 조치 | 확인 사항 |
|---|---|---|
| 1 | **오류 메시지 분석** | 실패 유형 판별 (Mock/Assertion/NPE/Bean) |
| 2 | **소스 코드 재확인** | 테스트 대상 메서드의 실제 동작 흐름 추적 |
| 3 | **Mock 설정 점검** | 소스 코드 내부 호출과 Mock 반환값 일치 여부 |
| 4 | **기대값 점검** | Mock 데이터 → 소스 로직 → 반환값 추적하여 기대값 수정 |
| 5 | **테스트 코드 수정** 후 재실행 | `./gradlew test --tests "{테스트클래스}" |
| 6 | 3회 이상 실패 시 **테스트 재생성** | 소스 코드 재분석부터 시작 |
