# CLAUDE.md - AI TDD Demo 프로젝트 컨텍스트

## 프로젝트 개요

농협(NH) 프로젝트 1차 적용을 위한 AI TDD 검증용 데모 프로젝트.
AI 에이전트가 `docs/ai-tdd/` 문서를 기반으로 Java 테스트 코드를 자율 생성하는 시스템을 검증한다.

## 기술 스택

- Java 1.8 (source/target) + Spring Boot 2.7.17 + Gradle 6.8.3 (Kotlin DSL)
- Spring Security + JWT (jjwt 0.11.5)
- MyBatis 2.3.2 + H2 (인메모리 DB)
- springdoc-openapi-ui 1.8.0 (Swagger UI)
- 테스트: JUnit 5 + Mockito + AssertJ + Spring Security Test
- 입력 검증: Bean Validation (javax.validation) + spring-boot-starter-validation
- 품질: JaCoCo 0.8.7 (라인 80%, 분기 70%) + PIT 1.7.4 (mutation 65%, junit5Plugin 0.15)

## 패키지 구조

```
nh.ai.tdd.demo
├── config/       SecurityConfig                     (Spring Security 설정)
├── security/     JwtTokenProvider,                   (JWT 토큰 생성/검증)
│                 JwtAuthenticationFilter             (Bearer 토큰 필터)
├── controller/   UserController, AuthController,     (@RestController)
│                 NoticeController
├── service/      UserService, AuthService,           (@Service)
│                 NoticeService
├── mapper/       UserMapper, NoticeMapper            (@Mapper, MyBatis 인터페이스)
├── domain/       User, Notice                       (엔티티)
├── dto/          CreateUserRequest, LoginRequest,    (요청/응답 DTO)
│                 LoginResponse, SignupRequest,
│                 SignupResponse, CreateNoticeRequest,
│                 UpdateNoticeRequest
├── util/         MaskingUtil                         (static 유틸, 순수 단위테스트 대상)
└── exception/    UserNotFoundException,              (예외)
                  DuplicateEmailException,
                  NoticeNotFoundException,
                  InvalidCredentialsException,
                  GlobalExceptionHandler              (@RestControllerAdvice)
```

## 주요 파일 경로

| 용도 | 경로 |
|---|---|
| 빌드 설정 | `build.gradle.kts` |
| 앱 설정 | `src/main/resources/application.yml` |
| DB 스키마 | `src/main/resources/schema.sql` |
| MyBatis XML | `src/main/resources/mapper/UserMapper.xml`, `NoticeMapper.xml` |
| 보안 설정 | `src/main/java/.../config/SecurityConfig.java` |
| JWT 토큰 | `src/main/java/.../security/JwtTokenProvider.java` |
| JWT 필터 | `src/main/java/.../security/JwtAuthenticationFilter.java` |
| 테스트 설정 | `src/test/resources/application.yml` |
| 에이전트 행동 지시서 | `docs/ai-tdd/ai-tdd-agent/SKILL.md` |
| 배치 실행 가이드 | `docs/ai-tdd/ai-tdd-agent/batch-execution.md` |
| 리뷰 에이전트 | `docs/ai-tdd/ai-tdd-review-agent/SKILL.md` |
| 공통 테스트 생성 가이드 | `docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md` |
| 레이어별 템플릿 | `docs/ai-tdd/ai-tdd-skills/templates/*.md` |
| 제약조건 | `docs/ai-tdd/ai-tdd-skills/constraints/*.md` |
| 참조 예제 | `docs/ai-tdd/ai-tdd-skills/references/examples/*.md` |
| 검증 절차 | `docs/ai-tdd/ai-tdd-skills/verification/*.md` |
| 성과 리포트 템플릿 | `docs/ai-tdd/ai-tdd-skills/verification/performance-report.md` |
| PRD v3 (최신) | `docs/plan/2026-02-23-prd-ai-tdd.md` |
| TRD v3 (최신) | `docs/plan/2026-02-23-trd-ai-tdd.md` |
| 폐쇄망 의존성 목록 | `docs/closed-network-dependencies.md` |

## 작업 이력

### 2026-02-23: 문서 고도화 + 데모 프로젝트 구성

**1. AI TDD 스킬 문서 자율 실행 수준 고도화**
- 6개 BLOCKING gap(B1~B6) 해결: 소스 탐색 알고리즘, 테스트 수 산출 공식, 파라미터→테스트 매핑, 메서드→테스트 변환, 기존 테스트 자율 처리, 에러 복구 의사결정 트리
- 수정 파일 6개: `ai-tdd-agent/SKILL.md`, `ai-tdd-skills/GENERATION-GUIDE.md`, 4개 템플릿

**2. PRD v3 / TRD v3 문서 작성**
- v2 대비 자율 실행 프로세스, 알고리즘 상세, 검증 방법 추가
- 변경 비교 리포트: `docs/plan/report3.md`

**3. 데모 프로젝트 구성 (Kotlin → Java 전환)**
- Spring Initializr Kotlin 프로젝트를 Java로 전환
- User 도메인 CRUD 구현 (Controller, Service, Mapper, Util, Exception)
- H2 + MyBatis 설정, Swagger UI 적용
- `application.properties` → `application.yml` 전환
- 빌드 성공 + 구동 확인 완료 (port 8080)

### 2026-02-24: 추가 문서 3종 작성

**1. ai-tdd-review-agent (리뷰 에이전트)**
- 생성된 테스트의 품질을 자동 검증하는 2차 에이전트
- 5단계 리뷰 프로세스: 파일 수집 → 구조 검증 → 품질 검증 → NH 규칙 → 리포트
- 정량 점수 산정 (구조 30% + 품질 40% + NH규칙 30%)
- 안티패턴 8종 자동 탐지 (AP1~AP8)

**2. 배치 실행 가이드**
- 패키지/폴더 단위 다중 클래스 일괄 테스트 생성
- 의존성 기반 실행 순서 (위상 정렬: Util → Mapper → Service → Controller)
- 규모별 전략 (소규모 5개 이하 / 중규모 6~20 / 대규모 20+)
- 실패 시 SKIP 후 계속 진행, 종합 리포트

**3. 성과 리포트 템플릿**
- Before/After 정량 비교 (커버리지, 테스트 수, 레벨 분포)
- 생산성 지표 (생성 시간, 자율 실행 성공률)
- 품질 지표 (리뷰 점수, 안티패턴, NH 규칙 준수율)
- 측정 방법 가이드 (JaCoCo, grep 명령)

### 2026-03-02: TRD v3 기준 버전 정합성 재구성

**데모 프로젝트 기술 스택을 TRD v3 (농협 로그트래커 환경) 기준으로 다운그레이드**
- Spring Boot 4.0.3 → 2.7.17, MyBatis 4.0.1 → 2.3.2
- springdoc-openapi 2.8.6 → 1.8.0
- Java 17 → 1.8 (source/target), Gradle 9.3.1 → 7.6.4
- Gradle 6.8.3(TRD 명시) 적용, gradle.properties에서 org.gradle.java.home=JDK 1.8 지정
- JaCoCo 0.8.11 + PIT 1.7.4 추가
- Java 8 호환성 수정: Map.of() → Collections.singletonMap(), String.repeat() → char[] 방식
- 빌드 성공 확인 완료

### 2026-03-02: JWT 인증 + Notice CRUD 확장

**AI TDD 검증 시나리오 다양화를 위한 데모 프로젝트 확장**

**1. JWT 인증 레이어 추가**
- Spring Security + JWT(jjwt 0.11.5) 의존성 추가
- JwtTokenProvider: 토큰 생성/검증/클레임 추출
- JwtAuthenticationFilter: Bearer 토큰 파싱 → SecurityContext 설정
- SecurityConfig: 공개(/api/auth/**, /swagger-ui/**, /h2-console/**) / 보호(/api/**) 경로 분리
- AuthService + AuthController: login(비밀번호 검증→토큰 발급), signup(BCrypt 인코딩→저장)

**2. Notice CRUD 추가 (간단한 도메인)**
- Notice 엔티티, NoticeMapper(Java+XML), NoticeService, NoticeController
- CRUD + 조회 시 viewCount 자동 증가

**3. 예외 처리 중앙화**
- GlobalExceptionHandler (@RestControllerAdvice)로 5개 예외 통합 처리
- UserController의 기존 @ExceptionHandler 3개 제거
- NoticeNotFoundException, InvalidCredentialsException 신규 추가

**4. DB 스키마/데이터 수정**
- users 테이블에 role 컬럼 추가, notices 테이블 신규 생성
- data.sql: BCrypt 인코딩 비밀번호, role 값, 공지사항 3건

**5. 테스트 설정**
- src/test/resources/application.yml 생성 (JWT secret 포함)
- 빌드 성공 확인 (`./gradlew clean build` 통과)

**수정 7개 + 신규 20개 = 총 27개 파일**

### 2026-03-09: Gradle 6.8.3 다운그레이드 + 소스 품질 보완

**1. Gradle 7.6.4 → 6.8.3 다운그레이드**
- gradle-wrapper.properties: 배포판 URL 변경, networkTimeout 제거 (7.x 전용)
- build.gradle.kts: JaCoCo report API `xml.required.set()` → `xml.isEnabled` (6.x 호환)
- gradle.properties 신규 생성: `org.gradle.java.home=JDK 1.8` (개발 머신 JDK 17 대응)

**2. 폐쇄망 환경 플러그인 버전 정합성**
- JaCoCo: 0.8.11 → 0.8.7 (반입 가능 버전)
- PITest junit5Plugin: 1.1.2 → 0.15 (반입 가능 버전)
- 폐쇄망 반입 필요 아티팩트 전체 목록 문서화: `docs/closed-network-dependencies.md`
- 플러그인 아티팩트명 오류 교정: `com.github.maiflai` → `info.solidsoft.gradle.pitest`, `pitest-core` → `pitest`

**3. DTO Bean Validation 추가**
- spring-boot-starter-validation 의존성 추가
- 5개 DTO에 @NotBlank, @Email, @Size, @Pattern 적용
- 3개 Controller에 @Valid 추가
- GlobalExceptionHandler에 MethodArgumentNotValidException 처리 추가 (400 + 필드별 오류)

**4. 기타 소스 보완**
- UserMapper.xml UPDATE에 password 필드 추가
- Notice.viewCount 기본값 0 초기화
- 빌드 성공 확인 (`./gradlew clean build` 통과)

**수정 13개 + 신규 2개 = 총 15개 파일**

## 다음 작업 예정

1. **AI TDD 문서 기반 테스트 자동 생성 검증**
   - `docs/ai-tdd/` 문서를 따라 각 레이어별 테스트 생성
   - 10개 클래스 대상 (난이도별):
     - 복잡: JwtTokenProvider, JwtAuthenticationFilter, AuthService, SecurityConfig
     - 중간: UserService, UserController, MaskingUtil
     - 간단: NoticeService, NoticeController, NoticeMapper
   - 생성된 테스트의 컴파일 → 실행 → 커버리지까지 확인

2. **테스트 생성 결과 피드백 → 문서 보정**
   - 실제 생성 과정에서 발견되는 문서 미비점 수정
   - 자율 실행 실패 케이스 분석 및 알고리즘 보완

## 빌드/실행 명령

```bash
./gradlew clean build              # 빌드
./gradlew bootRun                  # 실행 (port 8080)
./gradlew test                     # 테스트 실행
./gradlew jacocoTestReport         # 커버리지 리포트 생성
./gradlew jacocoTestCoverageVerification  # 커버리지 임계값 검증
./gradlew pitest                   # 뮤테이션 테스트
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
