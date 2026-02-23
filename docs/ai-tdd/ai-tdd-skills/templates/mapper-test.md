# 맵퍼 레이어 테스트 템플릿

> **대상**: `@Mapper`, MyBatis 맵퍼 인터페이스
> **테스트 방식**: Mock 기반 단위테스트 (`@ExtendWith(MockitoExtension.class)`)
> 또는 DB 연동 테스트 (`@MybatisTest`)

---

## 역할

당신은 MyBatis 기반 데이터 접근 계층의 테스트를 전문으로 하는 시니어 Java 테스트 엔지니어입니다.
JUnit 5, Mockito, AssertJ를 사용하여 맵퍼 인터페이스의 CRUD 동작을 검증하는 테스트를 생성합니다.

## 컨텍스트

다음은 테스트가 필요한 MyBatis 맵퍼 인터페이스입니다:

```java
[MAPPER_CLASS_CODE_HERE]
```

## 테스트 방식 선택 기준

| 방식 | 어노테이션 | 선택 기준 | 장점 |
|---|---|---|---|
| Mock 기반 | `@ExtendWith(MockitoExtension.class)` | 맵퍼를 호출하는 **서비스 테스트**에서 간접 검증 | 빠른 실행, DB 불필요 |
| DB 연동 | `@MybatisTest` | 맵퍼 자체의 **SQL 정확성** 직접 검증 | SQL 매핑, 파라미터 바인딩 검증 |

> 기본적으로 **Mock 기반**을 권장합니다. DB 연동 테스트는 SQL 매핑 검증이 반드시 필요한 경우에 사용합니다.

## 테스트 클래스 구조 (Mock 기반)

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class {맵퍼클래스명}Test {

    @Mock
    private {맵퍼인터페이스} {맵퍼명};

    // ── Level 1: Happy Case (40%) ──

    @Test
    @DisplayName("한글로 시나리오 설명")
    void should_{동작}_when_{조건}() {
        // Given
        {엔티티} expected = new {엔티티}(...);
        when({맵퍼}.{메서드}({인자})).thenReturn(expected);

        // When
        {엔티티} result = {맵퍼}.{메서드}({인자});

        // Then
        assertThat(result).isNotNull();
        assertThat(result.get{필드}()).isEqualTo({기대값});
    }

    // ── Level 2: Edge Case (30%) ──
    // ── Level 3: Exception (20%) ──
    // ── Level 4: Mutation Testing (10%) ──
}
```

## 테스트 클래스 구조 (DB 연동 - 선택적)

```java
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.*;

@MybatisTest
@Sql("/sql/{테스트데이터}.sql")  // 테스트 데이터 초기화
class {맵퍼클래스명}IntegrationTest {

    @Autowired
    private {맵퍼인터페이스} {맵퍼명};

    @Test
    @DisplayName("한글로 시나리오 설명")
    void should_{동작}_when_{조건}() {
        // Given - @Sql로 데이터 준비됨

        // When
        {엔티티} result = {맵퍼}.{메서드}({인자});

        // Then
        assertThat(result).isNotNull();
    }
}
```

## 요구사항

### 기본 요구사항
- 맵퍼 인터페이스의 모든 public 메서드에 대한 테스트 생성
- 기본: Mock 기반 단위테스트 (서비스 테스트에서 간접 검증)
- CRUD 각 연산에 대한 검증 패턴 적용
- Given-When-Then 패턴 적용

### 4단계 레벨별 요구사항

**Level 1 (40%): Happy Case**
- 각 CRUD 메서드의 정상 동작 검증
- 예시: findById 정상 조회, insert 성공, update 성공, delete 성공

**Level 2 (30%): Edge Case**
- 조회 결과 없음(null 반환), 빈 리스트 반환
- 파라미터 null, 0, 음수 등 경계값
- 예시: findById에 존재하지 않는 ID → null 반환

**Level 3 (20%): Exception**
- DB 오류 시나리오 (DataAccessException 등)
- 제약조건 위반 (중복 키, NOT NULL 위반 등)
- 예시: 중복 insert 시도 → DuplicateKeyException

**Level 4 (10%): Mutation Testing**
- 맵퍼 메서드 호출 여부/횟수 검증
- 전달된 파라미터값 정밀 검증 (ArgumentCaptor)
- 예시: insert에 전달된 엔티티의 필드값 검증

### 맵퍼 계층 특화 패턴

| CRUD 연산 | Mock 패턴 | 검증 포인트 |
|---|---|---|
| SELECT (단건) | `when(mapper.findById(id)).thenReturn(entity)` | 반환값 필드 검증 |
| SELECT (목록) | `when(mapper.findAll()).thenReturn(List.of(...))` | 리스트 크기, 요소 검증 |
| INSERT | `when(mapper.insert(entity)).thenReturn(1)` | 반환값(영향받은 행 수) 검증 |
| UPDATE | `when(mapper.update(entity)).thenReturn(1)` | 반환값 검증, 호출 인자 검증 |
| DELETE | `when(mapper.delete(id)).thenReturn(1)` | 반환값 검증 |
| COUNT | `when(mapper.count()).thenReturn(10)` | 정확한 카운트 검증 |

## 생성 알고리즘

에이전트가 맵퍼 인터페이스에서 테스트 코드를 기계적으로 변환하는 규칙입니다.

### CRUD 메서드 시그니처 → Mock 패턴 자동 선택

```
맵퍼 메서드명 패턴 매칭 → Mock/검증 패턴 자동 결정:

메서드명 패턴        → thenReturn 값           → 어설션 패턴
──────────────────────────────────────────────────────────────
find*, select*, get* (단건)
  → thenReturn(entity)   → assertThat(result).isNotNull() + 필드 검증

find*, select*, get* (List 반환)
  → thenReturn(List.of(entity1, entity2))
                          → assertThat(result).hasSize(2) + 요소 검증

insert*, save*, create*
  → thenReturn(1)        → assertThat(result).isEqualTo(1)

update*, modify*
  → thenReturn(1)        → assertThat(result).isEqualTo(1) + 인자 캡처 검증

delete*, remove*
  → thenReturn(1)        → assertThat(result).isEqualTo(1)

count*
  → thenReturn(N)        → assertThat(result).isEqualTo(N)
```

### Edge Case 자동 생성 규칙

```
SELECT 메서드:
  - 존재하지 않는 ID → thenReturn(null) → assertThat(result).isNull()
  - 빈 목록 → thenReturn(Collections.emptyList()) → assertThat(result).isEmpty()

INSERT/UPDATE/DELETE 메서드:
  - 대상 없음 → thenReturn(0) → assertThat(result).isZero()

파라미터가 Long/Integer:
  - null, 0, -1 테스트 추가
```

### DB 연동 테스트 선택 기준

```
판단 기준:
1. 프로젝트에 테스트용 DB 설정(H2/test profile)이 있는가?
2. SQL 매핑 XML이 복잡한가 (동적 SQL, 조인 등)?
3. 사용자가 DB 연동 테스트를 요청했는가?

→ 위 조건 중 하나라도 해당 → DB 연동 테스트 병행 고려
→ 기본값: Mock 기반 테스트
```

## 출력 형식

- 테스트 클래스명: `{원본클래스명}Test` (Mock 기반), `{원본클래스명}IntegrationTest` (DB 연동)
- 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명
- 레벨별 주석 구분: `// ── Level 1: Happy Case (40%) ──`

## 제약사항

- 테스트 데이터에 실제 개인정보 사용 금지 (마스킹된 더미 데이터 사용)
- 비밀번호 필드는 암호화된 형태로 테스트 데이터 구성
- DB 연동 테스트 시 `@Transactional`로 롤백 보장 (기본 적용됨)
- 상세 규칙은 `constraints/nh-rules.md` 참조
- 네이밍 규칙은 `constraints/naming-conventions.md` 참조
- 코드 스타일은 `constraints/code-style.md` 참조
