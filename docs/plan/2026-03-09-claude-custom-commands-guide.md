# Claude Code 커스텀 슬래시 커맨드 가이드

> **목적**: AI TDD 스킬 활용을 위한 커스텀 슬래시 커맨드 생성 방법 및 실전 예제
> **대상**: Claude Code CLI 사용자

---

## 1. 커스텀 슬래시 커맨드란?

Claude Code에서 `/명령어` 형태로 호출할 수 있는 사용자 정의 명령어.
`SKILL.md` 파일에 지시사항을 작성하면, Claude가 해당 절차를 따라 작업을 수행한다.

```
사용자: /generate-test UserService
Claude: UserService 클래스에 대해 AI TDD 테스트를 생성합니다...
```

---

## 2. 파일 구조

### 2.1 저장 위치

| 범위 | 경로 | 용도 |
|------|------|------|
| **프로젝트 전용** | `.claude/skills/{명령어명}/SKILL.md` | 팀 공유, Git 커밋 가능 |
| **개인 전역** | `~/.claude/skills/{명령어명}/SKILL.md` | 모든 프로젝트에서 사용 |

> 같은 이름이면 프로젝트 레벨이 개인 레벨보다 우선한다.

### 2.2 디렉토리 구조

```
.claude/skills/{명령어명}/
├── SKILL.md          ← 필수: 명령어 정의 (프론트매터 + 지시사항)
├── reference.md      ← 선택: 상세 참조 문서
├── examples.md       ← 선택: 예제 출력물
└── scripts/
    └── helper.sh     ← 선택: Claude가 실행할 스크립트
```

**핵심**: `SKILL.md` 파일 하나만 있으면 동작한다. 나머지는 필요할 때 추가.

---

## 3. SKILL.md 작성법

### 3.1 기본 구조

```yaml
---
name: 명령어이름
description: 이 명령어가 하는 일 (한 줄 설명)
---

여기에 Claude가 따를 지시사항을 작성한다.
마크다운 형식으로 자유롭게 작성 가능.
```

### 3.2 프론트매터 옵션 전체

```yaml
---
# 필수
name: generate-test                  # 명령어 이름 (소문자, 숫자, 하이픈만, 최대 64자)
description: 클래스 테스트 생성       # Claude가 자동 호출 판단에 사용하는 설명

# 선택 — 호출 제어
disable-model-invocation: true       # true: 사용자만 /명령어로 호출 가능
                                     # false(기본): Claude가 상황에 맞게 자동 호출 가능
user-invocable: true                 # true(기본): /명령어로 호출 가능
                                     # false: 배경 지식용 (호출 불가, 컨텍스트에만 로드)
argument-hint: [ClassName]           # /명령어 자동완성 시 표시되는 인자 힌트

# 선택 — 실행 환경
allowed-tools: Read, Grep, Glob      # 사용 가능한 도구 제한 (미지정 시 전체 허용)
model: claude-opus-4-6               # 사용할 모델 지정 (미지정 시 현재 모델)
context: fork                        # fork: 격리된 서브에이전트에서 실행
---
```

### 3.3 옵션별 상세 설명

#### `disable-model-invocation`

| 값 | 동작 | 사용 시점 |
|----|------|----------|
| `true` | `/명령어`로만 실행, Claude가 자동 호출 불가 | 배포, 커밋 등 부작용이 있는 명령어 |
| `false` (기본) | Claude가 대화 맥락에 맞으면 자동 호출 | 정보 조회, 분석 등 안전한 명령어 |

> **권장**: 파일을 수정하거나 외부에 영향을 주는 명령어는 반드시 `true`로 설정

#### `user-invocable`

| 값 | 동작 | 사용 시점 |
|----|------|----------|
| `true` (기본) | `/명령어`로 호출 가능 | 일반적인 커맨드 |
| `false` | 호출 불가, 배경 지식으로만 로드 | 코딩 컨벤션, 아키텍처 규칙 등 |

#### `allowed-tools`

Claude가 해당 명령어 실행 중 사용할 수 있는 도구를 제한한다.

```yaml
# 읽기 전용 명령어 (안전)
allowed-tools: Read, Grep, Glob

# 파일 수정도 허용
allowed-tools: Read, Grep, Glob, Edit, Write

# 전체 허용 (기본값, 미지정 시)
# Bash, Read, Write, Edit, Grep, Glob, Agent 등 모두 사용 가능
```

#### `context: fork`

격리된 서브에이전트에서 실행한다. 메인 대화 컨텍스트에 영향을 주지 않는다.

```yaml
# 코드베이스 탐색용 — 메인 컨텍스트를 오염시키지 않음
context: fork
```

---

## 4. 변수 치환

### 4.1 사용 가능한 변수

| 변수 | 설명 | 예시 |
|------|------|------|
| `$ARGUMENTS` | 전달된 인자 전체 | `/cmd foo bar` → `foo bar` |
| `$0` | 첫 번째 인자 | `/cmd foo bar` → `foo` |
| `$1` | 두 번째 인자 | `/cmd foo bar` → `bar` |
| `$N` | N번째 인자 (0부터) | `/cmd a b c` → `$2` = `c` |
| `${CLAUDE_SESSION_ID}` | 현재 세션 ID | 로깅용 |
| `${CLAUDE_SKILL_DIR}` | SKILL.md가 있는 디렉토리 경로 | 참조 파일 접근용 |

### 4.2 변수 사용 예시

**단일 인자:**

```yaml
---
name: analyze
description: 클래스 분석
argument-hint: [ClassName]
---

$ARGUMENTS 클래스를 분석하라.
```

```
/analyze UserService
→ "UserService 클래스를 분석하라."
```

**복수 인자:**

```yaml
---
name: migrate
description: 메서드를 다른 클래스로 이동
argument-hint: [SourceClass TargetClass methodName]
---

$0 클래스의 $2 메서드를 $1 클래스로 이동하라.
```

```
/migrate UserService AuthService validatePassword
→ "UserService 클래스의 validatePassword 메서드를 AuthService 클래스로 이동하라."
```

**참조 파일 접근:**

```yaml
---
name: generate-test
description: 테스트 생성
---

아래 참조 문서를 읽고 따르라:
- [생성 규칙](${CLAUDE_SKILL_DIR}/rules.md)
- [예제](${CLAUDE_SKILL_DIR}/examples.md)

$ARGUMENTS 클래스에 대해 테스트를 생성하라.
```

---

## 5. 참조 파일 활용

SKILL.md가 길어지면 별도 파일로 분리한다. SKILL.md 내에서 마크다운 링크로 참조하면 Claude가 자동으로 읽는다.

### 5.1 구조 예시

```
.claude/skills/generate-test/
├── SKILL.md              ← 핵심 절차 (간결하게)
├── generation-rules.md   ← 4-Level 생성 규칙 상세
├── nh-rules.md           ← NH 보안 규칙
└── template.md           ← 테스트 코드 템플릿
```

### 5.2 SKILL.md에서 참조

```yaml
---
name: generate-test
description: AI TDD 테스트 생성
---

$ARGUMENTS 클래스에 대해 테스트를 생성하라.

## 참조 문서
- [생성 규칙](generation-rules.md)을 따른다
- [NH 보안 규칙](nh-rules.md)을 최우선 적용한다
- [테스트 템플릿](template.md) 구조를 사용한다
```

> **팁**: SKILL.md는 500줄 이하로 유지하고, 상세 내용은 참조 파일로 분리

---

## 6. AI TDD용 커맨드 설계 예시 (4개)

이 프로젝트의 AI TDD 스킬에 맞춘 실전 커맨드 예시.

### 6.1 `/generate-test` — 단일 클래스 테스트 생성

```
.claude/skills/generate-test/SKILL.md
```

```yaml
---
name: generate-test
description: AI TDD 스킬 기반 단일 클래스 테스트 생성
disable-model-invocation: true
argument-hint: [ClassName]
---

$ARGUMENTS 클래스에 대해 AI TDD 테스트를 생성하라.

## 절차

1. `docs/ai-tdd/ai-tdd-agent/SKILL.md`의 7단계 프로세스를 따른다
2. `docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md`의 4-Level 규칙을 적용한다
3. `docs/ai-tdd/ai-tdd-skills/constraints/nh-rules.md` 보안 규칙을 최우선 적용한다

## 검증

4. `./gradlew compileTestJava` 컴파일 확인
5. `./gradlew test --tests "*.${ARGUMENTS}Test"` 실행 확인
6. 실패 시 SKILL.md의 에러 복구 의사결정 트리(C1~C4, T1~T4)를 따라 자동 수정
7. 최대 3회 재시도 후 결과 보고
```

**사용:**

```
/generate-test UserService
/generate-test NoticeController
/generate-test MaskingUtil
```

### 6.2 `/batch-test` — 배치 테스트 생성

```
.claude/skills/batch-test/SKILL.md
```

```yaml
---
name: batch-test
description: 패키지 또는 폴더 단위 배치 테스트 생성
disable-model-invocation: true
argument-hint: [package-or-folder]
---

$ARGUMENTS 범위의 클래스에 대해 배치 테스트를 생성하라.

## 절차

1. `docs/ai-tdd/ai-tdd-agent/batch-execution.md`를 따른다
2. 대상 클래스를 수집한다 (Application, Config, DTO, Exception 제외)
3. 의존성 기반 위상 정렬 순서로 실행한다
   - Utility (의존성 없음) → Mapper → Service → Controller
4. 클래스별 `generate-test` 절차를 적용한다
5. 개별 실패 시 3회 재시도 후 SKIP, 다음 클래스로 진행

## 리포트

6. 종합 리포트를 생성한다:
   - 성공/SKIP/실패 클래스 목록
   - 생성된 테스트 수 합계
   - 커버리지 요약
```

**사용:**

```
/batch-test service
/batch-test controller
/batch-test nh.ai.tdd.demo
```

### 6.3 `/review-test` — 생성된 테스트 품질 리뷰

```
.claude/skills/review-test/SKILL.md
```

```yaml
---
name: review-test
description: AI 리뷰 에이전트로 테스트 품질 검증
disable-model-invocation: true
argument-hint: [ClassName]
---

$ARGUMENTS 클래스의 테스트를 리뷰하라.

## 절차

1. `docs/ai-tdd/ai-tdd-review-agent/SKILL.md`의 5단계 프로세스를 따른다
   - Stage 1: 파일 수집 (테스트 + 소스)
   - Stage 2: 구조 검증 (테스트 수, 레벨 분포)
   - Stage 3: 품질 검증 (안티패턴 8종, Given-When-Then)
   - Stage 4: NH 규칙 검증 (PII, 암호화, 마스킹)
   - Stage 5: 리포트 생성

## 점수 산정

2. 점수를 산정한다: 구조(30%) + 품질(40%) + NH규칙(30%)
3. 90점 이상: PASS / 70~89: WARN / 70 미만: FAIL
4. FAIL인 경우 구체적인 개선 사항을 제시한다
```

**사용:**

```
/review-test UserService
/review-test NoticeController
```

### 6.4 `/tdd-cycle` — Red-Green-Refactor 사이클

```
.claude/skills/tdd-cycle/SKILL.md
```

```yaml
---
name: tdd-cycle
description: 신규 프로젝트용 Red-Green-Refactor TDD 사이클 실행
disable-model-invocation: true
argument-hint: [ClassName methodName]
---

$0 클래스의 $1 메서드에 대해 TDD 사이클을 실행하라.

## RED 단계

1. 실패하는 테스트 1개를 작성한다
2. `./gradlew compileTestJava` → 컴파일 성공 확인
3. `./gradlew test --tests "*.${0}Test"` → **실패 확인** (이것이 정상)
4. 실패 이유가 올바른지 확인한다 (UnsupportedOperationException 등)

## GREEN 단계

5. 테스트를 통과시키는 **최소한의 코드만** 구현한다
6. `./gradlew test --tests "*.${0}Test"` → **통과 확인**
7. 불필요한 코드가 추가되지 않았는지 확인한다

## REFACTOR 단계

8. 중복 제거, 변수명 개선, 메서드 추출 등 코드 품질을 개선한다
9. `./gradlew test --tests "*.${0}Test"` → **통과 재확인**
10. 새로운 기능이 추가되지 않았는지 확인한다

## 보고

11. 각 단계(RED/GREEN/REFACTOR) 결과를 명시적으로 보고한다
```

**사용:**

```
/tdd-cycle UserService createUser
/tdd-cycle NoticeService getNoticeById
```

---

## 7. 실전 팁

### 7.1 명령어 확인

```
# Claude Code에서 사용 가능한 명령어 목록 보기
/      ← 슬래시 입력 후 자동완성 목록 확인
```

### 7.2 명령어 이름 규칙

- 소문자, 숫자, 하이픈(`-`)만 사용
- 최대 64자
- 예: `generate-test`, `batch-test`, `review-test`, `tdd-cycle`

### 7.3 디버깅

명령어가 의도대로 동작하지 않을 때:

1. SKILL.md 문법 확인 (프론트매터 `---` 누락 여부)
2. 파일 경로 확인 (`.claude/skills/{이름}/SKILL.md` 정확한지)
3. `$ARGUMENTS` 치환이 올바른지 확인
4. `disable-model-invocation` 설정 확인

### 7.4 커맨드 vs Hook vs 플러그인 비교

| 구분 | 커스텀 커맨드 | Hook | 플러그인 |
|------|-------------|------|---------|
| 생성 난이도 | 낮음 (마크다운 1개) | 중간 (JSON + 셸 스크립트) | 높음 (패키지 구조) |
| 호출 방식 | `/명령어`로 수동 | 이벤트 발생 시 자동 | `/명령어` 또는 자동 |
| 용도 | 워크플로우 실행 | 자동 검증/포맷팅 | 복합 기능 제공 |
| 공유 | Git 커밋으로 팀 공유 | Git 커밋 또는 개인 설정 | npm/marketplace 배포 |
| 예시 | `/generate-test` | 저장 시 자동 lint | Superpowers, TDD Guard |

### 7.5 권장 시작 순서

```
1. /generate-test 먼저 만들어본다 (가장 단순)
2. 동작 확인 후 /review-test 추가
3. 배치가 필요하면 /batch-test 추가
4. 신규 프로젝트 적용 시 /tdd-cycle 추가
```

---

## 8. 폐쇄망 환경 참고

- 커스텀 커맨드는 `.claude/skills/` 폴더에 마크다운 파일만 있으면 동작한다
- 외부 네트워크 불필요 (npm install, brew install 등 없음)
- 프로젝트와 함께 Git으로 관리하면 폐쇄망에서도 그대로 사용 가능
- Claude Code CLI 자체만 설치되어 있으면 즉시 사용 가능
