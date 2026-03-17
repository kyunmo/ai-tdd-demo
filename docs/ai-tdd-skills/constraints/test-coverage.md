# 테스트 커버리지 요구사항

> 이 문서는 테스트 커버리지의 **목표 기준, 측정 방법, 미달 시 조치**를 정의합니다.

---

## 1. 커버리지 목표

**뮤테이션 커버리지란?**
소스 코드에 의도적으로 작은 변이(Mutation, 예: `if(a > b)`를 `if(a >= b)`로 변경)를 만들어,
기존 테스트 코드가 이 변화를 감지하고 실패하는지를 측정하는 테스트 품질 지표 입니다.
높은 뮤테이션 커버리지는 테스트가 코드의 동작을 매우 꼼꼼하게 검증하고 있음을 의미합니다.

### 1.1. 전체 기준

| 항목 | 최소 기준 | 목표 | 측정 도구 |
|---|---|---|---|
| 라인 커버리지 | **80%** | 90% | JaCoCo |
| 분기 커버리지 | **70%** | 80% | JaCoCo |
| 메서드 커버리지 | **100%** (public 메서드) | 100% | JaCoCo |
| 클래스 커버리지 | **100%** (테스트 대상 클래스) | 100% | JaCoCo |
| 뮤테이션 커버리지 | **65%** | 75% | PIT |

### 1.2. 계층별 기준

| 계층 | 라인 | 분기 | 비고 |
|---|---|---|---|
| Service | 85% | 75% | 비즈니스 로직이 집중, 높은 기준 |
| Controller | 80% | 70% | HTTP 응답 검증 중심 |
| Mapper | 75% | 65% | Mock 기반 시 간접 검증 허용 |
| Utility | 90% | 85% | 순수 함수, 높은 기준 적용 |

---

## 2. 측정 방법

### 2.1. JaCoCo 실행

```bash
# 테스트 실행 + 커버리지 보고서 생성
./gradlew test jacocoTestReport

# 보고서 위치
# build/reports/jacoco/test/html/index.html
```

### 2.2. build.gradle 설정 (참고)

```groovy
plugins {
    id 'jacoco'
}

jacoco {
    toolVersion = "0.8.7"
}

jacocoTestReport {
    reports {
        xml.enabled = true
        html.enabled = true
    }
}

jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                counter = 'LINE'
                value = 'COVEREDRATIO'
                minimum = 0.80
            }
            limit {
                counter = 'BRANCH'
                value = 'COVEREDRATIO'
                minimum = 0.70
            }
        }
    }
}

test {
    finalizedBy jacocoTestReport
}
```

### 2.3. PIT Mutation Testing (참고)

```groovy
plugins {
    id 'info.solidsoft.pitest' version '1.7.4'
}

pitest {
    targetClasses = ['com.nhcard.al.demo.*']
    targetTests = ['com.nhcard.al.demo.*']
    mutationThreshold = 65
    outputFormats = ['HTML']
}
```

```bash
# 뮤테이션 테스트 실행
./gradlew pitest

# 보고서 위치
# build/reports/pitest/index.html
```

---

## 3. 커버리지 미달 시 조치

### 3.1. 에이전트 조치

커버리지가 기준에 미달하면 다음 순서로 추가 테스트를 생성합니다.

| 순서 | 조치 | 기대 효과 |
|---|---|---|
| 1 | 미커버 분기(if/else)에 대한 Edge Case 추가 | 분기 커버리지 향상 |
| 2 | 미커버 메서드에 대한 Happy Case 추가 | 라인/메서드 커버리지 향상 |
| 3 | 미커버 예외 경로에 대한 Exception 테스트 추가 | 라인/분기 커버리지 향상 |
| 4 | verify 기반 Mutation 테스트 추가 | 뮤테이션 커버리지 향상 |

### 3.2. 커버리지 제외 대상

다음 항목은 커버리지 측정에서 제외할 수 있습니다.

| 제외 대상 | 이유 |
|---|---|
| DTO/VO 클래스 (getter/setter) | 자동 생성 코드, 로직 없음 |
| Configuration 클래스 | 설정 코드, 비즈니스 로직 없음 |
| Application 메인 클래스 | Spring Boot 진입점 |
| Lombok 생성 코드 | 자동 생성 코드 |

```groovy
// JaCoCo 제외 설정 예시
jacocoTestReport {
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.collect {
            fileTree(dir: it, exclude: [
                    '**/dto/**',
                    '**/config/**',
                    '**/*Application*'
            ])
        }))
    }
}
```

---

## 4. 커버리지 보고서 확인 항목

보고서 생성 후 다음을 확인합니다.

| 확인 항목 | 기준 |
|---|---|
| 전체 라인 커버리지 | 80% 이상 |
| 전체 분기 커버리지 | 70% 이상 |
| 미커버 클래스 목록 | 비즈니스 클래스가 없어야 함 |
| 미커버 메서드 목록 | public 메서드가 없어야 함 |
| 커버리지 추이 | 이전 대비 하락 없어야 함 |

---

## 5. 4단계 레벨과 커버리지 관계

| 레벨 | 기여하는 커버리지 |
|---|---|
| Level 1: Happy Case (각 메서드 1개) | 라인 커버리지 기본 확보, 메서드 커버리지 100% |
| Level 2: Edge Case | 분기 커버리지 향상 (null/경계값 분기) |
| Level 3: Exception (throw 문 1:1) | 예외 경로 라인/분기 커버리지 |
| Level 4: Mutation (각 메서드 1개 - 필수) | 호출 검증 (조건 변이 검출) |
