# CLAUDE.md - AI TDD Demo 프로젝트 컨텍스트

## 프로젝트 개요

농협(NH) 프로젝트 1차 적용을 위한 AI TDD 검증용 데모 프로젝트.
AI 에이전트가 `docs/ai-tdd/` 문서를 기반으로 Java 테스트 코드를 자율 생성하는 시스템을 검증한다.

## 기술 스택

- Java 17 + Spring Boot 4.0.3 + Gradle 9.3.1 (Kotlin DSL)
- MyBatis 4.0.1 + H2 (인메모리 DB)
- springdoc-openapi 2.8.6 (Swagger UI)
- 테스트: JUnit 5 + Mockito + AssertJ (Spring Boot test starter 포함)

## 패키지 구조

```
nh.ai.tdd.demo
├── controller/   UserController      (@RestController, @WebMvcTest 대상)
├── service/      UserService          (@Service, @ExtendWith(MockitoExtension) 대상)
├── mapper/       UserMapper           (@Mapper, MyBatis 인터페이스)
├── domain/       User                 (엔티티)
├── dto/          CreateUserRequest    (요청 DTO)
├── util/         MaskingUtil          (static 유틸, 순수 단위테스트 대상)
└── exception/    UserNotFoundException, DuplicateEmailException
```

## 주요 파일 경로

| 용도 | 경로 |
|---|---|
| 빌드 설정 | `build.gradle.kts` |
| 앱 설정 | `src/main/resources/application.yml` |
| DB 스키마 | `src/main/resources/schema.sql` |
| MyBatis XML | `src/main/resources/mapper/UserMapper.xml` |
| 에이전트 행동 지시서 | `docs/ai-tdd/ai-tdd-agent/SKILL.md` |
| 공통 테스트 생성 가이드 | `docs/ai-tdd/ai-tdd-skills/SKILL.md` |
| 레이어별 템플릿 | `docs/ai-tdd/ai-tdd-skills/templates/*.md` |
| 제약조건 | `docs/ai-tdd/ai-tdd-skills/constraints/*.md` |
| 참조 예제 | `docs/ai-tdd/ai-tdd-skills/references/examples/*.md` |
| 검증 절차 | `docs/ai-tdd/ai-tdd-skills/verification/*.md` |
| PRD v3 (최신) | `docs/plan/2026-02-23-prd-ai-tdd.md` |
| TRD v3 (최신) | `docs/plan/2026-02-23-trd-ai-tdd.md` |

## 작업 이력

### 2026-02-23: 문서 고도화 + 데모 프로젝트 구성

**1. AI TDD 스킬 문서 자율 실행 수준 고도화**
- 6개 BLOCKING gap(B1~B6) 해결: 소스 탐색 알고리즘, 테스트 수 산출 공식, 파라미터→테스트 매핑, 메서드→테스트 변환, 기존 테스트 자율 처리, 에러 복구 의사결정 트리
- 수정 파일 6개: `ai-tdd-agent/SKILL.md`, `ai-tdd-skills/SKILL.md`, 4개 템플릿

**2. PRD v3 / TRD v3 문서 작성**
- v2 대비 자율 실행 프로세스, 알고리즘 상세, 검증 방법 추가
- 변경 비교 리포트: `docs/plan/report3.md`

**3. 데모 프로젝트 구성 (Kotlin → Java 전환)**
- Spring Initializr Kotlin 프로젝트를 Java로 전환
- User 도메인 CRUD 구현 (Controller, Service, Mapper, Util, Exception)
- H2 + MyBatis 설정, Swagger UI 적용
- `application.properties` → `application.yml` 전환
- 빌드 성공 + 구동 확인 완료 (port 8080)

## 다음 작업 예정

1. **AI TDD 문서 기반 테스트 자동 생성 검증**
   - `docs/ai-tdd/` 문서를 따라 각 레이어별 테스트 생성
   - 4개 클래스 대상: UserController, UserService, UserMapper, MaskingUtil
   - 생성된 테스트의 컴파일 → 실행 → 커버리지까지 확인

2. **테스트 생성 결과 피드백 → 문서 보정**
   - 실제 생성 과정에서 발견되는 문서 미비점 수정
   - 자율 실행 실패 케이스 분석 및 알고리즘 보완

## 빌드/실행 명령

```bash
./gradlew clean build    # 빌드
./gradlew bootRun        # 실행 (port 8080)
./gradlew test           # 테스트 실행
```

## 컨벤션

- 테스트 클래스명: `{원본클래스명}Test`
- 테스트 메서드명: `should_{동작}_when_{조건}` (영문 snake_case)
- `@DisplayName`: 한글로 시나리오 설명
- 레벨별 주석 구분: `// ── Level 1: Happy Case (40%) ──`
- 개인정보 테스트 데이터: 반드시 더미 데이터 사용 (실제 PII 금지)

## GitHub

- Repository: https://github.com/kyunmo/ai-tdd-demo
- Branch: `main`
