# 변경 리포트 (2026-03-09, 2차)

> **목적**: 신규 프로젝트 TDD 도입 가이드 작성 + 파일명 리네이밍
> **환경**: JDK 1.8 + Gradle 6.8.3 + Spring Boot 2.7.17

---

## 1. 변경 요약

| 카테고리 | 변경 항목 | 파일 수 |
|---------|----------|--------|
| 신규 문서 | 신규 프로젝트 TDD 도입 절차 가이드 | 1 |
| 파일 리네이밍 | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` | 1 |
| 참조 경로 수정 | 리네이밍에 따른 참조 업데이트 | 8 |
| 리포트 | 본 리포트 | 1 |
| **합계** | | **11** |

---

## 2. 신규 문서: 신규 프로젝트 TDD 도입 절차 가이드

**파일**: `docs/plan/2026-03-09-new-project-tdd-adoption-guide.md`

현재 데모 프로젝트(레거시 대상 Test-After)와 달리, 신규(클린) 프로젝트에서 Red-Green-Refactor TDD를 적용하는 절차를 상세히 기술한 문서.

### 문서 구성

| 단계 | 제목 | 내용 |
|------|------|------|
| 1단계 | 프로젝트 초기 설정 | 스캐폴딩, AI TDD 문서 배치, `.claude.md` 커스터마이징, 품질 도구 설정 |
| 2단계 | 요구사항 → 테스트 케이스 설계 | 요구사항 분석, 인터페이스 선설계, 4-Level 테스트 케이스 목록 작성 |
| 3단계 | Red-Green-Refactor 사이클 | RED(실패 테스트 작성) → GREEN(최소 구현) → REFACTOR(설계 개선), 구체적 코드 예제 포함 |
| 4단계 | 레이어별 TDD 적용 순서 | Bottom-Up: Domain/DTO → Utility → Mapper → Service → Controller → Security |
| 5단계 | AI 에이전트 통합 | 시나리오 A(테스트만) / B(전체) / C(배치+구현), 기존 스킬 문서 재활용 방법 |
| 6단계 | 품질 게이트와 완료 기준 | 4단계 게이트(RED/GREEN/REFACTOR/커버리지), Definition of Done 체크리스트 |
| 7단계 | 실전 도입 로드맵 | Phase 1 파일럿(2주) → Phase 2 확대(2주) → Phase 3 정착(지속) |
| 부록 A | Red-Green-Refactor 체크리스트 | 매 사이클 확인용 체크리스트 |
| 부록 B | 자주 하는 실수와 대응 | 6가지 흔한 실수 + 대응 방법 |
| 부록 C | 기존 문서 수정 포인트 | 레거시 데모 → 신규 TDD 전환 시 수정 필요한 문서 목록 |

### 핵심 포인트

- **레거시 vs 신규 차이**: 입력이 "소스 코드"에서 "요구사항/인터페이스"로 바뀌는 것이 본질적 차이
- **기존 스킬 문서 재활용**: 4-Level 구조, 템플릿, 제약조건, 검증 절차는 그대로 사용 가능
- **AI 에이전트 시나리오 C(배치+구현) 권장**: 인터페이스 확정 후 배치로 테스트 생성 → 개발자가 구현

---

## 3. 파일 리네이밍

### 3.1 변경 대상

| Before | After | 이유 |
|--------|-------|------|
| `docs/ai-tdd/ai-tdd-skills/SKILL.md` | `docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md` | 에이전트 폴더의 `SKILL.md`와 이름 중복, 역할이 "테스트 생성 규칙"이므로 구분 |

### 3.2 변경하지 않은 파일과 이유

| 파일 | 유지 이유 |
|------|----------|
| `ai-tdd-agent/SKILL.md` | 에이전트 폴더에 복사하는 프롬프트 파일, 파일명 자유 |
| `ai-tdd-review-agent/SKILL.md` | 동일 |
| `ai-tdd-skills/.claude.md` | Claude Code 자동 인식 파일 (디렉토리 컨텍스트), 변경 불가 |

---

## 4. 참조 경로 수정 내역

리네이밍에 따라 `ai-tdd-skills/SKILL.md`를 참조하는 모든 파일의 경로를 `GENERATION-GUIDE.md`로 업데이트.

| # | 파일 | 수정 위치 | 변경 내용 |
|---|------|----------|----------|
| 1 | `CLAUDE.md` | 58행 (파일 경로 테이블) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 2 | `CLAUDE.md` | 74행 (작업 이력) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 3 | `README.md` | 57행 (디렉토리 구조) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 4 | `docs/report.md` | 18행 (파일 목록) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 5 | `docs/ai-tdd/ai-tdd-agent/SKILL.md` | 193행 (6단계 참조) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 6 | `docs/ai-tdd/ai-tdd-agent/SKILL.md` | 268행 (Edge Case 참조) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 7 | `docs/ai-tdd/README.md` | 316행 (Mermaid 다이어그램) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 8 | `docs/plan/2026-02-23-prd-ai-tdd.md` | 69행 (변경 파일 테이블) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 9 | `docs/plan/2026-02-23-trd-ai-tdd.md` | 230행 (섹션 제목) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |
| 10 | `docs/plan/report3.md` | 135행 (섹션 제목) | `ai-tdd-skills/SKILL.md` → `GENERATION-GUIDE.md` |

---

## 5. 폐쇄망 수동 적용 가이드

폐쇄망 환경에서 이 변경사항을 직접 적용할 때의 순서.

### Step 1: 파일 리네이밍

```bash
# SKILL.md → GENERATION-GUIDE.md 이름 변경
mv docs/ai-tdd/ai-tdd-skills/SKILL.md docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md
```

### Step 2: 참조 경로 일괄 치환

아래 10개 파일에서 `ai-tdd-skills/SKILL.md`를 `ai-tdd-skills/GENERATION-GUIDE.md`로 텍스트 치환한다.

```bash
# Linux/Mac 환경
sed -i 's|ai-tdd-skills/SKILL\.md|ai-tdd-skills/GENERATION-GUIDE.md|g' \
  CLAUDE.md \
  README.md \
  docs/ai-tdd/README.md \
  docs/ai-tdd/ai-tdd-agent/SKILL.md \
  docs/plan/2026-02-23-prd-ai-tdd.md \
  docs/plan/2026-02-23-trd-ai-tdd.md \
  docs/plan/report3.md \
  docs/report.md
```

### Step 3: 신규 파일 복사

아래 2개 파일을 폐쇄망 프로젝트에 복사한다.

| 파일 | 위치 |
|------|------|
| `2026-03-09-new-project-tdd-adoption-guide.md` | `docs/plan/` |
| `report5.md` | `docs/plan/` |

### Step 4: 검증

```bash
# 잔여 참조 확인 (결과 0건이어야 정상)
grep -r "ai-tdd-skills/SKILL\.md" --include="*.md" .

# GENERATION-GUIDE.md 파일 존재 확인
ls docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md
```

---

## 6. 전체 변경 파일 목록

### 신규 (2개)

| # | 파일 | 내용 |
|---|------|------|
| 1 | `docs/plan/2026-03-09-new-project-tdd-adoption-guide.md` | 신규 프로젝트 TDD 도입 절차 가이드 |
| 2 | `docs/plan/report5.md` | 본 리포트 |

### 리네이밍 (1개)

| # | Before | After |
|---|--------|-------|
| 3 | `docs/ai-tdd/ai-tdd-skills/SKILL.md` | `docs/ai-tdd/ai-tdd-skills/GENERATION-GUIDE.md` |

### 참조 경로 수정 (8개)

| # | 파일 | 변경 내용 |
|---|------|----------|
| 4 | `CLAUDE.md` | `SKILL.md` → `GENERATION-GUIDE.md` (2곳) |
| 5 | `README.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |
| 6 | `docs/report.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |
| 7 | `docs/ai-tdd/ai-tdd-agent/SKILL.md` | `SKILL.md` → `GENERATION-GUIDE.md` (2곳) |
| 8 | `docs/ai-tdd/README.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |
| 9 | `docs/plan/2026-02-23-prd-ai-tdd.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |
| 10 | `docs/plan/2026-02-23-trd-ai-tdd.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |
| 11 | `docs/plan/report3.md` | `SKILL.md` → `GENERATION-GUIDE.md` (1곳) |

---

## 7. AI TDD 활용 추천 Claude Code 플러그인

AI TDD 스킬 문서를 활용한 테스트 생성/검증 워크플로우에 도움이 되는 플러그인 목록.

### 7.1 핵심 추천 (3개)

#### Superpowers

- **GitHub**: https://github.com/obra/superpowers
- **설치**: `/plugin` → Discover 탭에서 검색
- **역할**: TDD 가이드 + 설계 + 디버깅 + 코드 리뷰 통합 스킬 프레임워크
- **주요 기능**:
  - TDD 스킬: Red-Green-Refactor 사이클 강제, YAGNI 원칙 적용
  - Brainstorming: 코드 작성 전 설계 검증
  - Systematic Debugging: 테스트 실패 시 체계적 디버깅 (3회 실패 시 아키텍처 리뷰)
  - Subagent-driven Development: 병렬 에이전트로 구현 + 2단계 리뷰
  - Verification Before Completion: 완료 주장 전 검증 강제
- **AI TDD 시너지**: 테스트 생성 전 설계 검증, 생성 후 품질 확인까지 전 과정 가이드

#### TDD Guard

- **GitHub**: https://github.com/nizos/tdd-guard
- **설치**: `brew install tdd-guard` 또는 `npm install --save-dev tdd-guard`
- **역할**: TDD 원칙을 Hook 레벨에서 **물리적으로 차단**
- **주요 기능**:
  - 테스트 없이 구현 코드 작성 시 → 블록
  - 테스트 범위를 초과하는 과도한 구현 시 → 블록
  - 한번에 여러 테스트 추가 시 → 블록
  - Write, Edit, MultiEdit 작업을 인터셉트하여 TDD 위반 감지
- **지원 프레임워크**: Jest, Vitest, Storybook, pytest, PHPUnit, Go, Rust
- **주의**: JUnit(Java) 공식 지원 여부 확인 필요 — 현재 JS/Python/Go/Rust 위주
- **AI TDD 시너지**: 신규 프로젝트 Red-Green-Refactor 사이클에서 구현 선행 방지

#### Code Review (Anthropic 공식)

- **URL**: https://claude.com/plugins/code-review
- **설치**: `/plugin` → Discover 탭에서 검색
- **역할**: PR 리뷰를 5개 병렬 에이전트로 자동 수행
- **주요 기능**:
  - CLAUDE.md 규칙 준수 확인
  - 버그 탐지
  - 히스토리 컨텍스트 분석
  - PR 히스토리 분석
  - 코드 코멘트 자동 생성
- **실행**: `/code-review` 명령
- **AI TDD 시너지**: AI로 생성된 테스트의 품질을 코드 리뷰 단계에서 추가 검증

### 7.2 보조 추천 (2개)

#### Feature Dev (Anthropic 공식)

- **URL**: https://claude.com/plugins/feature-dev
- **역할**: 기능 개발 워크플로우 가이드 (탐색 → 설계 → 구현 → 리뷰)
- **AI TDD 시너지**: 신규 프로젝트에서 설계 → 구현 흐름을 체계화, 인터페이스 선설계 단계 지원

#### ATDD (Acceptance Test Driven Development)

- **GitHub**: https://github.com/swingerman/atdd
- **역할**: 인수 테스트 주도 개발 — 상위 레벨 인수 테스트 → 하위 레벨 단위 테스트 TDD 사이클
- **AI TDD 시너지**: 4-Level 테스트 중 Level 1(Happy Case)을 인수 테스트 관점에서 보강

### 7.3 추천 조합

```
┌─────────────────────────────────────────────────────┐
│              AI TDD 워크플로우 플러그인 조합          │
│                                                      │
│  ┌──────────────┐  ┌───────────┐  ┌──────────────┐  │
│  │ Superpowers  │→ │ TDD Guard │→ │ Code Review  │  │
│  │              │  │           │  │              │  │
│  │ 설계 검증    │  │ TDD 강제  │  │ PR 품질 검증 │  │
│  │ + TDD 가이드 │  │ (Hook)    │  │ (5 에이전트) │  │
│  └──────────────┘  └───────────┘  └──────────────┘  │
│                                                      │
│  Phase: 설계 단계     구현 단계      리뷰 단계       │
└─────────────────────────────────────────────────────┘
```

- **Superpowers**: 설계 + TDD 사이클 가이드 (소프트 가이드)
- **TDD Guard**: 구현 중 TDD 위반 물리적 차단 (하드 게이트)
- **Code Review**: PR 단계에서 최종 품질 검증

### 7.4 폐쇄망 환경 참고

폐쇄망에서는 플러그인 자동 설치가 불가하므로, GitHub에서 소스를 다운로드하여 로컬 설치해야 한다.

```bash
# 로컬 플러그인 설치 방법
claude plugin add /path/to/local-plugin-directory
```

플러그인별 의존성(Node.js, npm 패키지 등)도 함께 반입이 필요하다.
