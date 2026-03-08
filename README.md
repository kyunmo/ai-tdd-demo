# AI TDD Demo

AI 에이전트 기반 테스트 자동 생성 시스템의 검증용 데모 프로젝트입니다.
프로젝트에 1차 적용하기 위한 구조를 기반으로 구성되어 있습니다.

## 프로젝트 목적

`docs/ai-tdd/` 에 정의된 AI TDD 스킬 문서들이 실제로 동작하는지 검증합니다.

- AI 에이전트가 Java 소스 코드를 분석하여 **자율적으로 테스트 코드를 생성**할 수 있는가?
- 생성된 테스트가 **컴파일 → 실행 → 커버리지 확인**까지 무중단으로 완료되는가?
- 4개 레이어(Controller, Service, Mapper, Util) 각각의 템플릿이 올바르게 적용되는가?

## 기술 스택

| 항목 | 버전 |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Gradle | 9.3.1 (Kotlin DSL) |
| MyBatis | 4.0.1 |
| H2 Database | 인메모리 |
| Swagger UI | springdoc-openapi 2.8.6 |

## 프로젝트 구조

```
ai-tdd-demo/
├── src/main/java/nh/ai/tdd/demo/
│   ├── AiTddDemoApplication.java        # Spring Boot 메인
│   ├── controller/
│   │   └── UserController.java          # REST API (6 endpoints)
│   ├── service/
│   │   └── UserService.java             # 비즈니스 로직 (6 methods)
│   ├── mapper/
│   │   └── UserMapper.java              # MyBatis 맵퍼 인터페이스 (7 methods)
│   ├── domain/
│   │   └── User.java                    # 엔티티
│   ├── dto/
│   │   └── CreateUserRequest.java       # 요청 DTO
│   ├── util/
│   │   └── MaskingUtil.java             # 개인정보 마스킹 유틸 (3 static methods)
│   └── exception/
│       ├── UserNotFoundException.java
│       └── DuplicateEmailException.java
├── src/main/resources/
│   ├── application.yml                  # Spring Boot 설정
│   ├── schema.sql                       # H2 테이블 DDL
│   ├── data.sql                         # 초기 데이터
│   └── mapper/
│       └── UserMapper.xml               # MyBatis SQL 매핑
├── src/test/java/nh/ai/tdd/demo/
│   └── AiTddDemoApplicationTests.java   # 컨텍스트 로드 테스트
├── docs/
│   ├── ai-tdd/                          # AI TDD 스킬 문서 (핵심)
│   │   ├── ai-tdd-agent/SKILL.md        # 에이전트 행동 지시서
│   │   ├── ai-tdd-skills/GENERATION-GUIDE.md  # 공통 테스트 생성 가이드
│   │   ├── ai-tdd-skills/templates/     # 레이어별 테스트 템플릿 (4종)
│   │   ├── ai-tdd-skills/constraints/   # 제약조건 (코드스타일, 네이밍, NH규칙)
│   │   ├── ai-tdd-skills/references/    # 참조 예제 (4종)
│   │   └── ai-tdd-skills/verification/  # 검증 절차 (컴파일, 실행, 커버리지)
│   └── plan/                            # 기획/설계 문서
│       ├── PRD-AI-TDD.md                # PRD 원본
│       ├── 2026-02-10-prd-ai-tdd.md     # PRD v2
│       ├── 2026-02-23-prd-ai-tdd.md     # PRD v3 (최신)
│       ├── 2026-02-11-trd-ai-tdd.md     # TRD v2
│       └── 2026-02-23-trd-ai-tdd.md     # TRD v3 (최신)
└── build.gradle.kts
```

## 데모 도메인: 사용자 관리

농협 프로젝트 구조에 맞춘 사용자(User) CRUD API입니다.

### API 엔드포인트

| 메서드 | 경로 | 설명 |
|---|---|---|
| GET | `/api/users` | 전체 사용자 조회 |
| GET | `/api/users/{id}` | 사용자 단건 조회 |
| POST | `/api/users` | 사용자 등록 |
| PUT | `/api/users/{id}` | 사용자 수정 |
| DELETE | `/api/users/{id}` | 사용자 삭제 |
| GET | `/api/users/{id}/masked-phone` | 마스킹된 전화번호 조회 |

### AI TDD 검증 대상 클래스

| 레이어 | 클래스 | 테스트 템플릿 | 핵심 검증 포인트 |
|---|---|---|---|
| Controller | `UserController` | `controller-test.md` | MockMvc, HTTP 상태코드, JSON 응답 |
| Service | `UserService` | `service-test.md` | Mock 의존성, 예외 분기, 비즈니스 로직 |
| Mapper | `UserMapper` | `mapper-test.md` | CRUD Mock 패턴, SQL 매핑 |
| Utility | `MaskingUtil` | `util-test.md` | ParameterizedTest, 경계값, NH 마스킹 |

## 실행 방법

```bash
# 빌드
./gradlew clean build

# 실행
./gradlew bootRun
```

### 접근 URL (localhost:8080)

- Swagger UI: `/swagger-ui.html`
- API Docs: `/v3/api-docs`
- H2 Console: `/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, user: `sa`)

## AI TDD 문서 체계

`docs/ai-tdd/` 디렉토리에 AI 에이전트가 테스트를 자율 생성하기 위한 전체 문서가 포함되어 있습니다.

```
에이전트 실행 흐름:
  클래스명 입력 → 소스 탐색 → 소스 분석 → 레이어 판별
  → 템플릿 선택 → 테스트 생성 → 컴파일 → 실행 → 커버리지 확인
```

상세 문서 관계는 `docs/ai-tdd/ai-tdd-skills/document-guide.md`를 참조하세요.
