# AI TDD DemoProject

> **목적**: AI TDD 스킬 문서를 활용하여 신규/레거시 프로젝트에 Red-Green-Refactor TDD를 도입
>
> **전제**: 현재 데모 프로젝트(DemoProject)는 레거시 프로젝트 대상 "테스트 후행 생성"을 검증하는 사전 작업임

## 프로젝트 목적

`docs/ai-tdd-skills/`에 정의된 AI 테스트 생성 문서들이 실제로 동작하는지 검증합니다.

- AI에이전트가 Java 소스 코드를 분석하여 **자율적으로 테스트 코드를 생성**할 수 있는가?
- 생성된 테스트가 **컴파일 → 실행 → 커버리지 확인**까지 완료되는가?
- 4개 레이어(Controller, Service, Mapper, Util) 각각의 템플릿이 올바르게 적용되는가?

### 레거시 vs 신규 프로젝트: 근본적 차이

| 구분 | 레거시 (데모) | 신규 프로젝트 |
|---|---|---|
| 코드 존재 여부 | 이미 구현됨 | 아직 없음 |
| 테스트 작성 시점 | 구현 후 (Test-After) | 구현 전 (Test-First) |
| AI 에이전트 역할 | 소스 분석 → 테스트 생성 | 요구사항 분석 → 테스트 생성 → 구현 가이드 |
| Red-Green-Refactor | 적용 불가 (이미 Green 상태) | 사이클 적용 |
| 설계 영향 | 없음 (기존 설계 따름) | 테스트가 설계를 주도 |

---

## 기술 스택

| 항목 | 버전 |
|---|---|
| java | 1.8 |
| Spring Boot | 2.7.17 |
| Gradle | 6.8.3 |
| MyBatis | 2.3.1 |
| H2 Database | 인메모리 |
| Swagger UI | springdoc-openapi 1.7.0 |

---

## 프로젝트 구조

```
demo-project
├── src/main/java/com/nhcard/al/demo/
│   ├── DemoApplication.java                # Spring Boot 메인
│   ├── config/
│   ├── controller/                         # REST API (endpoint)
│   ├── domain/                             # 엔티티
│   ├── dto/                                # DTO
│   ├── exception/
│   ├── mapper/                             # Mybatis 맵퍼 인터페이스
│   ├── security/
│   ├── service/                            # 비즈니스 로직
│   └── util/
│
├── src/main/resources/
│   ├── application.yml                     # Spring Boot 설정
│   ├── schema.sql                          # 테이블 DDL
│   ├── data.sql                            # 초기 데이터
│   └── mapper/                             # Mybatis SQL 매핑
│
├── src/test/java/com/nhcard/al/demo/       # 테스트 패키지
│
├── docs/       
│   ├── ai-tdd-skills/                      # 스킬문서 핵심
│   └── work-history/                       # 일자별 작업기록
│
├── .claude/agents/
│   └── test-generator.md                   # 에이전트 행동 지시서(*)
│
└── build.gradle
```

---

## 데모 도메인

인증(Auth), 사용자(User), 공지사항(Notice) CRUD API

### API 엔드포인트

http://localhost:8080/swagger-ui/index.html 참고

### AI 테스트 검증 대상 클래스

| 레이어 | 테스트 템플릿 | 핵심 검증 포인트 (현재) |
|---|---|---|
| Controller | `controller-test.md` | MockMvc, HTTP 상태코드, JSON 응답 |
| Service | `service-test.md` | Mock 의존성, 예외 분기, 비즈니스 로직 |
| Mapper | `mapper-test.md` | CRUD Mock 패턴, SQL 매핑 |
| Utility | `util-test.md` | ParameterizedTest, 경계값 등 |

> **[참고]** 현재 JaCoCo 라이브러리 반입 대기 중으로, 커버리지 검증은 일시적으로 보류됩니다.
> 테스트 생성 후에는 `verification/run-compile-test.sh` 스크립트를 통해 **컴파일 및 테스트 실행**만 자동 검증됩니다.

---

## 실행 방법

### IDE tool 이용 추천 (IntelliJ, Eclipse 등)

현재 간단한 구조로 작성되었기 때문에 별다른 설정 없음

```
1. Gradle update >  의존 소스 다운로드
2. DemoApplication > Gradle tool 이용 bootRun
```

### gradle이 전역으로 설정된 경우 이용

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 접근 URL (localhost:8080)

- Swagger UI : `/swagger-ui.html`
- H2 Console : `/h2-console`

---

## AI (Claude Code)

### AI 에이전트 아키텍처

이 프로젝트는 단일 AI 에이전트가 모든 작업을 처리하는 범용적인 구조 대신, **역할과 책임이 명확히 분리된 멀티 에이전트 시스템**을 지향합니다. 이를 위해 다음과 같은 아키텍처 원칙을 적용합니다.

**1. 중앙 허브와 모듈화된 스킬 분리:**
- 프로젝트 루트의 `CLAUDE.md` 파일은 프로젝트의 전체 개요와 진행 상황을 요약하고, 사용 가능한 AI 에이전트 목록과 그 사용법을 안내하는 **중앙 허브** 역할을 합니다.
- 각 에이전트의 구체적인 작동 방식, 규칙, 템플릿 등 상세한 지식 베이스는 `docs/ai-tdd-skills/`와 같이 **독립된 디렉토리에 모듈화하여 분리**합니다. 이는 소프트웨어 설계의 '관심사 분리' 원칙을 적용한 것입니다.

**2. 확장성과 유지보수성:**
- 이 구조는 '테스트 생성', '코드 리뷰' 등 각기 다른 임무를 가진 에이전트들을 독립적으로 추가하고 관리할수 있게 해줍니다.
- '코드 리뷰 에이전트'가 새로 추가될 경우, 루트 `CLAUDE.md`에 해당 에이전트의 사용법을 간단히 추가하고, 상세 스킬은 `docs/ai-review/` 디렉토리에 생성하는 방식으로 확장할 수 있어 유지보수성을 확보합니다.
※ 이번 프로젝트에서는 `docs/`디렉터리를 사용하여 프로젝트에서만(personal) 사용하게 제한하였고 전역(Global)에서 사용하도록 하는건 추후 고려합니다.

**3. 명확한 컨텍스트와 상호작용:**
- `test-generator`와 같은 특정 에이전트는 자신의 모듈(`docs/ai-tdd-skills`) 내 스킬 문서만 참조하면 되므로, 작업 컨텍스트가 명확해지고 다른 에이전트의 지침과 충돌할 위험을 줄입니다.
- 궁극적으로는 위 구조 위에 커스텀 명령어(예: `/generate-test`, `/review-code`)를 도입하여, 사용자가 루트 `CLAUDE.md`의 가이드를 보고 간단한 명령어로 원하는 에이전트와 스킬셋을 활성화하도록 만드는것을 목표로 합니다.

결론적으로, AI 아키텍처는 루트 `CLAUDE.md`를 고수준의 라이팅 가이드로 활용하고, 각 에이전트의 상세 구현은 자체 스킬 모듈에 위임하는 확장 가능한 모델을 채택합니다.

### AI 에이전트 확인

`.claude/agents/` 디렉토리에 `test-generator.md` 파일이 위치해야 합니다.

```
# 클로드 실행
claude

# 프로젝트 에이전트 목록에서 test-generator 확인
/agents
-----
❯ Create new agent
  
  Project agents (~\demo-project\.claude\agents)   
  test-generator · inherit
-----
```

### AI TDD 문서 체계

`docs/ai-tdd-skills/` 디렉토리에 AI 에이전트가 테스트를 생성하기 위한 전체 문서가 포함되어 있습니다.

```
에이전트 실행 흐름 (현재):
    {클래스명} test-generator 에이전트 활용 테스트 파일 생성 입력
    → 소스 분석 → 레이어 판별 → 템플릿 선택 → 테스트 생성
    → 자동 검증 스크립트 실행 (컴파일 → 실행)
```
> **[참고]** 현재 JaCoCo 라이브러리 반입 대기 중으로, 커버리지 확인은 일시적으로 보류됩니다.
> 모든 검증은 `verification/run-compile-test.sh` 스크립트를 통해 자동화됩니다.
