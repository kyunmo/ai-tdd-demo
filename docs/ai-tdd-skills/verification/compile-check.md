> **[문서 역할 안내] 문제 해결 가이드: 컴파일 오류**
>
> 이 문서는 AI 에이전트가 자동화 검증 스크립트(`run-compile-test.sh`) 실행 중 **컴파일 오류로 실패했을 때, 그 원인을 심층 분석하고 해결하기 위해 참조**하는 문제 해결 가이드입니다.
>
> 평상시 테스트 생성 시에는 이 문서의 명령어를 직접 실행하지 마십시오. 오직 자동화 스크립트가 실패했을 때만 이 가이드를 참조하여 문제를 해결하십시오.
---
# 컴파일 체크 검증

> 생성된 테스트 코드가 **컴파일 오류 없이 빌드**되는지 검증합니다.
> 테스트 실행 전 반드시 이 단계를 먼저 통과해야 합니다.

---

## 1. 검증 명령어

### 1.1. 전체 테스트 컴파일

```bash
./gradlew compileTestJava
```

### 1.2. 특정 클래스만 컴파일 확인

```bash
# 전체 컴파일 후 특정 클래스 오류 확인
./gradlew compileTestJava 2>&1 | grep -A 5 "UserServiceTest"
```

### 1.3. clean 후 컴파일 (캐시 문제 의심 시)

```bash
./gradlew clean compileTestJava
```

---

## 2. 합격/불합격 기준

| 기준 | 합격 | 불합격 | 
|---|---|---|
| 컴파일 결과 | `BUILD_SUCCESSFUL` | `BUILD_FAILED` |
| 오류 수 | 0개 | 1개 이상 |
| 경고 | 허용 (단, deprecation 경고는 확인) | - |

---

## 3. 자주 발생하는 컴파일 오류 및 해결

### 3.1. import 누락

```
error: cannot find symbol
    symbol: class Mock
    location: class UserServiceTest
```

**원인**: `@Mock`, `@InjectMocks` 등의 import가 누락됨

**해결**:
```java
// 누락되기 쉬운 import 목록
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
```

### 3.2. 타입 불일치

```
error: incompatible types: String cannot be converted to Long
    when(userMapper.findById("1")).thenReturn(user);
```

**원인**: 메서드 파라미터 타입과 테스트 데이터 타입 불일치

**해결**: 소스 코드의 메서드 시그니처를 확인하고 타입을 맞춤
```java
// 나쁜 예시
when(userMapper.findById("1")).thenReturn(user);    // String 전달

// 좋은 예시
when(userMapper.findById(1L)).thenReturn(user);     // Long 전달
```

### 3.3. 어노테이션 오류

```
error: annotation @WebMvcTest is not applicable to this element
```

**원인**: 계층에 맞지 않는 어노테이션 사용

**해결**:

| 계층 | 올바른 어노테이션 |
|---|---|
| Service | `@ExtendWith(MockitoExtension.class` |
| Controller | `@WebMvcTest({대상}Controller.class)` |
| Mapper (Mock) | `@ExtendWith(MockitoExtension.class)` |
| Mapper (DB) | `@MybatisTest` |
| Utility | 없음 (Pure JUnit 5) |

### 3.4. Mock 주입 오류

```
error: cannot find symbol
    symbol: method when(void)
```

**원인**: `void` 반환 메서드에 `when().thenReturn()` 사용

**해결**:
```java
// 나쁜 예시 - void 메서드에 when 사용
when(userMapper.insert(user)).thenReturn(null);

// 좋은 예시 - void 메서드는 doNothing 또는 생략
doNothing().when(userMapper).insert(any(User.class));
// 또는 void 메서드는 별도 stubbing 불필요 (기본 동작이 아무것도 안 함)
```

### 3.5. 접근 제한자 오류

```
error: getUserById(String) has private access in UserService
```

**원인**: private 메서드를 직접 테스트하려 함

**해결**: private 메서드는 public 메서드를 통해 간접 테스트
```java
// 나쁜 예시 - private 메서드 직접 호출
userService.validateEmail("test@example.com");  // private

// 좋은 예시 - public 메서드를 통한 간접 테스트
userService.registerUser(request);  // 내부에서 validateEmail 호출
```

### 3.6. 제네릭 타입 오류

```
error: incompatible types: List<Object> cannot be converted to List<User>
```

**원인**: Mock 반환값의 제네릭 타입 불일치

**해결**:
```java
// 나쁜 예시
when(userMapper.findAll()).thenReturn(new ArrayList());

// 좋은 예시
when(userMapper.findAll()).thenReturn(Arrays.asList(user1, user2));
// 또는
when(userMapper.findAll()).thenReturn(new ArrayList<User>());
```

### 3.7. Spring 테스트 의존성 누락

```
error: package org.springframework.test.web.servlet does not exist
```

**원인**: `build.gradle`에 Spring Boot Test 의존성 누락

**해결**:
```groovy
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

---

## 4. 계층별 필수 의존성 체크

### 4.1. 공통 (모든 계층)

```groovy
testImplementation 'org.springframework.boot:spring-boot-starter-test'
// 포함: JUnit 5, Mockito, AssertJ
```

### 4.2. Controller 추가

```groovy
// spring-boot-starter-test에 포함되어 있음
// MockMvc, @WebMvcTest 사용 가능
```

### 4.3. Mapper (DB 연동 시)

```groovy
testImplementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter-test'
// @MybatisTest 사용 가능
```

---

## 5. 불합격 시 에이전트 조치

컴파일 오류 발생 시 다음 순서로 해결합니다.

| 순서 | 조치 | 대상 |
|---|---|---|
| 1 | 오류 메시지에서 **파일명과 라인 번호** 확인 | 오류 위치 특정 |
| 2 | **import 누락** 여부 확인 → 필요한 import 추가 | 3.1. 참조 |
| 3 | **타입 불일치** 확인 → 소스 코드 메서드 시그니처 재확인 | 3.2. 참조 |
| 4 | **어노테이션 오류** 확인 → 계층별 올바른 어노테이션 적용 | 3.3. 참조 |
| 5 | 위 조치 후 재컴파일 → `./gradlew compileTestJava` | 재검증 |
| 6 | 3회 이상 실패 시 **소스 코드 재분석** 후 테스트 재생성 | 근본 원인 해결 |
