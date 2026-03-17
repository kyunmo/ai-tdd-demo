# 신규 프로젝트 TDD 도입 가이드

> **대상**: 새로운 프로젝트를 시작하면서 처음부터 TDD를 적용하려는 개발자
>
> **전제**: Claude Code 설치 완료, 폐쇄망 환경

---

## 1. Why — 왜 처음부터 TDD인가

### 1.1. "나중에 테스트 작성"이 실패하는 이유

많은 프로젝트가 이런 계획을 세웁니다:

> "일단 기능 먼저 만들고, 나중에 테스트 추가하자"

그리고 대부분 이렇게 됩니다:

```
일정 압박 → "이번 스프린트는 기능 우선" → 테스트 미작성
    → 코드 복잡도 증가 → 테스트 작성 비용 상승 → "다음에..."
    → 반복 → 테스트 없는 레거시 탄생
```

| "나중에" 방식의 문제 | TDD 방식의 해결 |
|---|---|
| 구현 후 테스트를 작성하면, 테스트하기 어려운 코드가 이미 완성됨 | 테스트를 먼저 쓰면, 자연스럽게 테스트하기 쉬운 구조가 됨 |
| 테스트 작성이 "추가 작업"으로 느껴져 우선순위에서 밀림 | 테스트가 개발 프로세스의 일부이므로 별도 시간 불필요 |
| 테스트 없이 배포 → 버그 발생 → 핫픽스 비용 | 테스트가 회귀 방지 → 안정적 배포 |

### 1.2. TDD가 설계를 개선하는 원리

TDD의 핵심 가치는 "테스트가 설계를 이끈다"는 것입니다.

```
"테스트를 먼저 작성한다"는 것은
"이 코드가 어떻게 사용될지 먼저 생각한다"는 뜻입니다.
```

테스트를 먼저 쓰면 자연스럽게:
- **인터페이스부터 설계**하게 됩니다 (구현이 아닌 사용자 관점)
- **의존성이 주입 가능**한 구조가 됩니다 (Mock으로 대체해야 하니까)
- **단일 책임**을 지키게 됩니다 (테스트 하나 = 검증 하나)
- **불필요한 코드를 안 쓰게** 됩니다 (테스트가 요구하는 것만 구현)

### 1.3. AI 에이전트 + TDD = 생산성과 품질 동시 확보

TDD의 가장 큰 진입 장벽은 **"테스트 코드를 매번 직접 쓰는 시간"**입니다.
AI 에이전트가 이 병목을 해결합니다.

| TDD 단계 | 기존 방식 | AI 에이전트 활용 |
|---|---|---|
| **Red** (테스트 작성) | 개발자가 수동 작성 (시간 소요) | 에이전트가 요구사항 → 테스트 자동 생성 |
| **Green** (구현) | 개발자가 구현 | 개발자가 구현 (변경 없음) |
| **Refactor** (개선) | 수동 리팩토링 | 리뷰 에이전트가 개선점 제안 |

결과적으로: **TDD의 이점은 유지하면서, 테스트 작성 비용을 대폭 줄임**

---

## 2. What — 무엇을 할 것인가

### 2.1. Red-Green-Refactor 사이클 (실패-통과-개선)

TDD는 짧은 사이클을 빠르게 반복합니다.

```
  ┌─────────────────────────────────────────────────────┐
  │                                                     │
  │   ① Red           ② Green          ③ Refactor      │
  │   테스트 작성      최소 구현         코드 개선        │
  │   (실패 확인)      (통과 확인)       (통과 유지)      │
  │                                                     │
  │   ──────→ ──────→ ──────→ ① Red (다음 기능)...     │
  │                                                     │
  └─────────────────────────────────────────────────────┘
```

**중요한 규칙**:
- Red 단계에서 테스트가 실패하는 것을 **반드시 확인** (이미 통과하면 의미 없는 테스트)
- Green 단계에서 **최소한의** 코드만 작성 (과잉 구현 금지)
- Refactor 단계에서 테스트가 **계속 통과**하는지 확인

### 2.2. AI 에이전트가 각 단계에서 하는 역할

| 단계 | AI 에이전트 역할 |
|---|---|
| **Red** | 요구사항을 분석하여 테스트 코드 자동 생성 |
| **Green** | 구현 코드 작성은 개발자가 수행 (설계 주도권은 사람에게) |
| **Refactor** | tdd-review 에이전트가 품질 검증 + 개선점 제안 |

### 2.3. 최종 목표

```
"테스트 없는 코드는 미완성 코드" — 이것이 팀의 기본 인식이 되는 것
```

---

## 3. How — 환경 세팅

### 3.1. 신규 프로젝트 생성

Spring Boot 프로젝트를 생성합니다 (Spring Initializr 또는 사내 아키타입 사용).

```
기술 스택 (예시):
- Java 1.8
- Spring Boot 2.7.x
- Gradle 6.8.3
- MyBatis
- H2 (개발/테스트용)
```

프로젝트 기본 구조:

```
board-project/
├── src/main/java/com/nhcard/al/board/
│   ├── BoardApplication.java
│   ├── config/
│   ├── controller/
│   ├── service/
│   ├── mapper/
│   ├── domain/
│   ├── dto/
│   └── exception/
├── src/main/resources/
│   ├── application.yml
│   ├── schema.sql
│   └── mapper/
├── src/test/java/com/nhcard/al/board/
└── build.gradle
```

### 3.2. 에이전트 및 스킬 문서 배치

레거시 가이드의 3장과 동일한 구조로 배치합니다.

```
board-project/
├── .claude/
│   └── agents/
│       ├── test-generator.md     ← 테스트 생성 에이전트
│       └── tdd-review.md         ← 테스트 리뷰 에이전트
│
└── docs/
    └── ai-tdd-skills/            ← 스킬 문서 전체 복사
        ├── .claude.md            ← [수정필요] 프로젝트 설정
        ├── generation-guide.md
        ├── templates/
        ├── constraints/
        ├── references/examples/
        └── verification/
```

> 에이전트/스킬 문서의 상세한 역할과 구조는 [레거시 TDD 가이드](./legacy-tdd-guide.md)의 **3.2절 에이전트 역할**, **3.3절 스킬 문서** 참조
> 에이전트의 실제 사용 방법(호출, 입력, 전환)은 [레거시 TDD 가이드](./legacy-tdd-guide.md)의 **3.7절 에이전트 사용법** 참조

#### .claude.md 수정

```markdown
| 항목 | 값 |
|---|---|
| 프로젝트명 | Board Project |
| 기본 패키지 | `com.nhcard.al.board` |
| 프레임워크 버전 | 2.7.17 |
| JDK 버전 | 1.8 |
```

### 3.3. 빌드 설정

`build.gradle`에 테스트 관련 의존성과 플러그인을 추가합니다.

```groovy
plugins {
    id 'java'
    id 'jacoco'
    id 'org.springframework.boot' version '2.7.17'
    id 'io.spring.dependency-management' version '1.0.15.RELEASE'
}

sourceCompatibility = '1.8'
targetCompatibility = '1.8'

// 테스트 의존성
dependencies {
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

// JaCoCo 설정
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
                minimum = 0.80
            }
            limit {
                counter = 'BRANCH'
                minimum = 0.70
            }
        }
    }
}
```

#### 동작 확인

```bash
# Claude Code 실행
claude

# 에이전트 목록 확인 (목록 확인용, 에이전트 호출과는 무관)
/agents
```

```
❯ Create new agent

  Project agents (~\board-project\.claude\agents)
  test-generator · inherit
  tdd-review · inherit
```

> **참고**: `/agents` 명령어는 에이전트 **목록 확인 및 생성/수정**용입니다. 에이전트 호출 방법은 다음 절(3.4)을 참조하세요.

### 3.4. 에이전트 사용법 (빠른 요약)

> 상세한 사용법은 [레거시 TDD 가이드](./legacy-tdd-guide.md)의 **3.7절** 참조

```
> test-generator, BoardService.java
```

에이전트가 자동으로 소스 탐색 → 분석 → 테스트 생성 → 컴파일 → 실행 → 커버리지 확인을 수행합니다.

신규 프로젝트 TDD에서는 아직 소스 코드가 없으므로, **요구사항과 함께** 입력합니다:

```
> test-generator, BoardService 테스트 코드 생성

요구사항:
- 게시글 등록: 제목, 내용, 작성자를 입력받아 게시글 생성
- 게시글 조회: ID로 조회 (조회수 자동 증가)
- 게시글 목록: 전체 목록 조회
- 게시글 수정: 제목, 내용 수정
- 게시글 삭제: ID로 삭제
- 존재하지 않는 게시글 접근 시 예외 발생
```

에이전트가 요구사항을 기반으로 테스트 코드를 생성합니다.
이 테스트는 아직 구현이 없으므로 컴파일 실패(Red 상태)가 정상입니다.

---

## 4. How — 게시판 CRUD TDD 실습

### 4.1. 요구사항 정의

게시판(Board) 기본 CRUD 기능을 TDD로 개발합니다.

```
기능 요구사항:
  1. 게시글 등록: 제목, 내용, 작성자를 입력받아 게시글을 생성한다
  2. 게시글 조회: ID로 게시글을 조회한다 (조회수 자동 증가)
  3. 게시글 목록: 전체 게시글 목록을 조회한다
  4. 게시글 수정: 제목, 내용을 수정한다
  5. 게시글 삭제: ID로 게시글을 삭제한다
  6. 예외처리: 존재하지 않는 게시글 조회/수정/삭제 시 예외 발생
```

### 4.2. Service 계층 TDD

Service 계층부터 시작합니다. 이것이 TDD의 핵심 영역입니다.

---

#### 사이클 1: 게시글 등록

##### Red — 테스트 먼저 작성

에이전트에게 요구사항을 주고 테스트를 생성합니다.

```
> test-generator, BoardService 테스트 코드 생성

요구사항:
- 게시글 등록: 제목, 내용, 작성자를 입력받아 게시글을 생성
- 게시글 조회: ID로 조회 (조회수 자동 증가)
- 게시글 목록: 전체 목록 조회
- 게시글 수정: 제목, 내용 수정
- 게시글 삭제: ID로 삭제
- 존재하지 않는 게시글 조회/수정/삭제 시 예외 발생
```

에이전트가 다음 파일을 생성합니다 (아래는 예상 출력이며, 실제 생성 코드는 모델에 따라 다를 수 있습니다):

```
src/test/java/com/nhcard/al/board/service/BoardServiceTest.java
```

```java
package com.nhcard.al.board.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nhcard.al.board.domain.Board;
import com.nhcard.al.board.dto.CreateBoardRequest;
import com.nhcard.al.board.dto.UpdateBoardRequest;
import com.nhcard.al.board.exception.BoardNotFoundException;
import com.nhcard.al.board.mapper.BoardMapper;

import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private BoardMapper boardMapper;

    @InjectMocks
    private BoardService boardService;

    private static final Long TEST_BOARD_ID = 1L;
    private static final String TEST_TITLE = "테스트 제목";
    private static final String TEST_CONTENT = "테스트 내용";
    private static final String TEST_AUTHOR = "테스트작성자";

    // ── Level 1: Happy Case ──
    @Nested
    @DisplayName("Level 1: Happy Case")
    class HappyCases {

        @Test
        @DisplayName("유효한 요청으로 게시글 등록 성공")
        void should_createBoard_when_validRequest() {
            // Given
            CreateBoardRequest request = new CreateBoardRequest();
            request.setTitle(TEST_TITLE);
            request.setContent(TEST_CONTENT);
            request.setAuthor(TEST_AUTHOR);

            when(boardMapper.insert(any(Board.class))).thenReturn(1);

            // When
            boardService.createBoard(request);

            // Then
            verify(boardMapper).insert(any(Board.class));
        }

        @Test
        @DisplayName("ID로 게시글 조회 성공")
        void should_getBoard_when_validId() {
            // Given
            Board testBoard = new Board();
            testBoard.setId(TEST_BOARD_ID);
            testBoard.setTitle(TEST_TITLE);
            testBoard.setViewCount(0);
            when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(testBoard);

            // When
            Board result = boardService.getBoardById(TEST_BOARD_ID);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo(TEST_TITLE);
            verify(boardMapper).increaseViewCount(TEST_BOARD_ID);
        }

        @Test
        @DisplayName("전체 게시글 목록 조회 성공")
        void should_getAllBoards_when_requested() {
            // Given
            Board testBoard1 = new Board();
            Board testBoard2 = new Board();
            when(boardMapper.findAll()).thenReturn(Arrays.asList(testBoard1, testBoard2));

            // When
            List<Board> result = boardService.getAllBoards();

            // Then
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("게시글 수정 성공")
        void should_updateBoard_when_validRequest() {
            // Given
            Board existingBoard = new Board();
            existingBoard.setId(TEST_BOARD_ID);
            when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(existingBoard);

            UpdateBoardRequest request = new UpdateBoardRequest();
            request.setTitle("수정된 제목");
            request.setContent("수정된 내용");

            // When
            boardService.updateBoard(TEST_BOARD_ID, request);

            // Then
            verify(boardMapper).update(any(Board.class));
        }

        @Test
        @DisplayName("게시글 삭제 성공")
        void should_deleteBoard_when_validId() {
            // Given
            Board existingBoard = new Board();
            existingBoard.setId(TEST_BOARD_ID);
            when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(existingBoard);

            // When
            boardService.deleteBoard(TEST_BOARD_ID);

            // Then
            verify(boardMapper).deleteById(TEST_BOARD_ID);
        }
    }

    // ── Level 3: Exception ──
    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외 발생")
    void should_throwException_when_boardNotFound() {
        // Given
        when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> boardService.getBoardById(TEST_BOARD_ID))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 수정 시 예외 발생")
    void should_throwException_when_updateNonExistentBoard() {
        // Given
        when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(null);
        UpdateBoardRequest request = new UpdateBoardRequest();

        // When & Then
        assertThatThrownBy(() -> boardService.updateBoard(TEST_BOARD_ID, request))
                .isInstanceOf(BoardNotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 삭제 시 예외 발생")
    void should_throwException_when_deleteNonExistentBoard() {
        // Given
        when(boardMapper.findById(TEST_BOARD_ID)).thenReturn(null);

        // When & Then
        assertThatThrownBy(() -> boardService.deleteBoard(TEST_BOARD_ID))
                .isInstanceOf(BoardNotFoundException.class);
    }
}
```

이제 컴파일을 시도합니다:

```bash
./gradlew compileTestJava
```

```
> Task :compileTestJava FAILED

error: package com.nhcard.al.board.domain does not exist
error: package com.nhcard.al.board.dto does not exist
error: package com.nhcard.al.board.exception does not exist
error: package com.nhcard.al.board.mapper does not exist
error: package com.nhcard.al.board.service does not exist
error: cannot find symbol: class Board
error: cannot find symbol: class BoardService
...

BUILD FAILED in 2s
```

**Red** 상태 확인. 아직 아무것도 구현하지 않았으니 당연합니다. 이제 하나씩 구현합니다.

##### Green — 최소 구현

테스트를 통과시키기 위한 최소한의 코드를 작성합니다.

**1) 도메인 클래스**

```java
// src/main/java/com/nhcard/al/board/domain/Board.java
package com.nhcard.al.board.domain;

public class Board {
    private Long id;
    private String title;
    private String content;
    private String author;
    private int viewCount;

    // getter, setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}
```

**2) DTO 클래스**

```java
// src/main/java/com/nhcard/al/board/dto/CreateBoardRequest.java
package com.nhcard.al.board.dto;

public class CreateBoardRequest {
    private String title;
    private String content;
    private String author;
    // getter, setter
}

// src/main/java/com/nhcard/al/board/dto/UpdateBoardRequest.java
package com.nhcard.al.board.dto;

public class UpdateBoardRequest {
    private String title;
    private String content;
    // getter, setter
}
```

**3) 예외 클래스**

```java
// src/main/java/com/nhcard/al/board/exception/BoardNotFoundException.java
package com.nhcard.al.board.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException(String message) {
        super(message);
    }
}
```

**4) Mapper 인터페이스**

```java
// src/main/java/com/nhcard/al/board/mapper/BoardMapper.java
package com.nhcard.al.board.mapper;

import com.nhcard.al.board.domain.Board;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface BoardMapper {
    Board findById(Long id);
    List<Board> findAll();
    int insert(Board board);
    int update(Board board);
    int deleteById(Long id);
    void increaseViewCount(Long id);
}
```

**5) Service 구현**

```java
// src/main/java/com/nhcard/al/board/service/BoardService.java
package com.nhcard.al.board.service;

import com.nhcard.al.board.domain.Board;
import com.nhcard.al.board.dto.CreateBoardRequest;
import com.nhcard.al.board.dto.UpdateBoardRequest;
import com.nhcard.al.board.exception.BoardNotFoundException;
import com.nhcard.al.board.mapper.BoardMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoardService {

    private final BoardMapper boardMapper;

    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public void createBoard(CreateBoardRequest request) {
        Board board = new Board();
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        board.setAuthor(request.getAuthor());
        boardMapper.insert(board);
    }

    public Board getBoardById(Long id) {
        Board board = boardMapper.findById(id);
        if (board == null) {
            throw new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + id);
        }
        boardMapper.increaseViewCount(id);
        return board;
    }

    public List<Board> getAllBoards() {
        return boardMapper.findAll();
    }

    public void updateBoard(Long id, UpdateBoardRequest request) {
        Board board = boardMapper.findById(id);
        if (board == null) {
            throw new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + id);
        }
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        boardMapper.update(board);
    }

    public void deleteBoard(Long id) {
        Board board = boardMapper.findById(id);
        if (board == null) {
            throw new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + id);
        }
        boardMapper.deleteById(id);
    }
}
```

이제 테스트를 실행합니다:

```bash
./gradlew test --tests "com.nhcard.al.board.service.BoardServiceTest"
```

```
> Task :test

BoardServiceTest > Level 1: Happy Case > 유효한 요청으로 게시글 등록 성공 PASSED
BoardServiceTest > Level 1: Happy Case > ID로 게시글 조회 성공 PASSED
BoardServiceTest > Level 1: Happy Case > 전체 게시글 목록 조회 성공 PASSED
BoardServiceTest > Level 1: Happy Case > 게시글 수정 성공 PASSED
BoardServiceTest > Level 1: Happy Case > 게시글 삭제 성공 PASSED
BoardServiceTest > 존재하지 않는 게시글 조회 시 예외 발생 PASSED
BoardServiceTest > 존재하지 않는 게시글 수정 시 예외 발생 PASSED
BoardServiceTest > 존재하지 않는 게시글 삭제 시 예외 발생 PASSED

8 tests completed, 8 passed

BUILD SUCCESSFUL in 5s
```

**Green** 상태 달성.

##### Refactor — 개선

테스트가 통과하는 상태를 유지하면서 코드를 개선합니다.

예를 들어, Service에서 반복되는 "게시글 조회 + null 체크" 로직을 private 메서드로 추출:

```java
private Board findBoardOrThrow(Long id) {
    Board board = boardMapper.findById(id);
    if (board == null) {
        throw new BoardNotFoundException("게시글을 찾을 수 없습니다. ID: " + id);
    }
    return board;
}
```

리팩토링 후 테스트 재실행:

```bash
./gradlew test --tests "com.nhcard.al.board.service.BoardServiceTest"
```

```
8 tests completed, 8 passed
BUILD SUCCESSFUL
```

Green 유지 확인. Service 계층 TDD 1사이클 완료.

---

### 4.3. Controller 계층 TDD

Service 구현이 완료되었으면, Controller를 TDD로 개발합니다.

##### Red — 테스트 먼저 작성

```
> test-generator, BoardController 테스트 코드 생성

요구사항:
- POST /api/boards: 게시글 등록 (201 Created)
- GET /api/boards/{id}: 게시글 조회 (200 OK)
- GET /api/boards: 게시글 목록 (200 OK)
- PUT /api/boards/{id}: 게시글 수정 (200 OK)
- DELETE /api/boards/{id}: 게시글 삭제 (204 No Content)
- 존재하지 않는 게시글 접근 시 404 Not Found
```

에이전트가 생성하는 Controller 테스트 예시:

```java
package com.nhcard.al.board.controller;

import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhcard.al.board.domain.Board;
import com.nhcard.al.board.dto.CreateBoardRequest;
import com.nhcard.al.board.exception.BoardNotFoundException;
import com.nhcard.al.board.service.BoardService;

@WebMvcTest(BoardController.class)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoardService boardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/boards - 게시글 등록 성공 (201)")
    void should_createBoard_when_validRequest() throws Exception {
        // Given
        CreateBoardRequest request = new CreateBoardRequest();
        request.setTitle("테스트 제목");
        request.setContent("테스트 내용");
        request.setAuthor("테스트작성자");

        // When & Then
        mockMvc.perform(post("/api/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());

        verify(boardService).createBoard(any(CreateBoardRequest.class));
    }

    @Test
    @DisplayName("GET /api/boards/{id} - 게시글 조회 성공 (200)")
    void should_getBoard_when_validId() throws Exception {
        // Given
        Board testBoard = new Board();
        testBoard.setId(1L);
        testBoard.setTitle("테스트 제목");
        when(boardService.getBoardById(1L)).thenReturn(testBoard);

        // When & Then
        mockMvc.perform(get("/api/boards/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 제목"));
    }

    @Test
    @DisplayName("GET /api/boards/{id} - 존재하지 않는 게시글 (404)")
    void should_return404_when_boardNotFound() throws Exception {
        // Given
        when(boardService.getBoardById(999L))
                .thenThrow(new BoardNotFoundException("게시글을 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/boards/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
```

컴파일 실패 (Red) → Controller 구현 (Green) → 리팩토링 (Refactor) 사이클을 반복합니다.

##### Green — Controller 구현

```java
@RestController
@RequestMapping("/api/boards")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createBoard(@Valid @RequestBody CreateBoardRequest request) {
        boardService.createBoard(request);
    }

    @GetMapping("/{id}")
    public Board getBoardById(@PathVariable Long id) {
        return boardService.getBoardById(id);
    }

    @GetMapping
    public List<Board> getAllBoards() {
        return boardService.getAllBoards();
    }

    @PutMapping("/{id}")
    public void updateBoard(@PathVariable Long id,
                             @Valid @RequestBody UpdateBoardRequest request) {
        boardService.updateBoard(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBoard(@PathVariable Long id) {
        boardService.deleteBoard(id);
    }
}
```

```bash
./gradlew test --tests "com.nhcard.al.board.controller.BoardControllerTest"
```

```
BoardControllerTest > POST /api/boards - 게시글 등록 성공 (201) PASSED
BoardControllerTest > GET /api/boards/{id} - 게시글 조회 성공 (200) PASSED
BoardControllerTest > GET /api/boards/{id} - 존재하지 않는 게시글 (404) PASSED
...

BUILD SUCCESSFUL
```

---

### 4.4. Mapper 계층 TDD

Mapper는 Mock 기반 단위테스트로 진행합니다. Service 테스트에서 Mapper를 Mock으로 사용하고 있지만, Mapper 자체의 인터페이스 계약도 별도로 검증합니다.

##### Red — 테스트 먼저 작성

```
> test-generator, BoardMapper 테스트 코드 생성
```

에이전트가 생성하는 Mapper 테스트 예시:

```java
package com.nhcard.al.board.mapper;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.nhcard.al.board.domain.Board;
import java.util.Arrays;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class BoardMapperTest {

    @Mock
    private BoardMapper boardMapper;

    // ── Level 1: Happy Case ──
    @Nested
    @DisplayName("Level 1: Happy Case")
    class HappyCases {

        @Test
        @DisplayName("ID로 게시글 조회 성공")
        void should_findBoard_when_validId() {
            // Given
            Board testBoard = new Board();
            testBoard.setId(1L);
            testBoard.setTitle("테스트 제목");
            when(boardMapper.findById(1L)).thenReturn(testBoard);

            // When
            Board result = boardMapper.findById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("테스트 제목");
        }

        @Test
        @DisplayName("게시글 등록 성공")
        void should_insertBoard_when_validBoard() {
            // Given
            Board testBoard = new Board();
            when(boardMapper.insert(any(Board.class))).thenReturn(1);

            // When
            int result = boardMapper.insert(testBoard);

            // Then
            assertThat(result).isEqualTo(1);
        }
    }

    // ── Level 2: Edge Case ──
    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
    void should_returnNull_when_boardNotExists() {
        // Given
        when(boardMapper.findById(999L)).thenReturn(null);

        // When
        Board result = boardMapper.findById(999L);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("게시글이 없으면 빈 목록 반환")
    void should_returnEmptyList_when_noBoardExists() {
        // Given
        when(boardMapper.findAll()).thenReturn(Collections.emptyList());

        // When & Then
        assertThat(boardMapper.findAll()).isEmpty();
    }

    // ── Level 4: Mutation Testing ──
    @Test
    @DisplayName("insert에 전달된 게시글 필드값 정밀 검증")
    void should_passCorrectFields_when_insertBoard() {
        // Given
        ArgumentCaptor<Board> captor = ArgumentCaptor.forClass(Board.class);
        Board testBoard = new Board();
        testBoard.setTitle("테스트 제목");
        testBoard.setContent("테스트 내용");

        // When
        boardMapper.insert(testBoard);

        // Then
        verify(boardMapper).insert(captor.capture());
        Board captured = captor.getValue();
        assertThat(captured.getTitle()).isEqualTo("테스트 제목");
        assertThat(captured.getContent()).isEqualTo("테스트 내용");
    }
}
```

이 시점에서 컴파일하면 **Red** 상태입니다 (BoardMapper 인터페이스가 아직 없으므로).

##### Green — Mapper 인터페이스 구현

앞서 Service TDD 과정에서 이미 BoardMapper 인터페이스를 생성했으므로, 테스트가 바로 통과합니다:

```bash
./gradlew test --tests "com.nhcard.al.board.mapper.BoardMapperTest"
```

```
BoardMapperTest > Level 1: Happy Case > ID로 게시글 조회 성공 PASSED
BoardMapperTest > Level 1: Happy Case > 게시글 등록 성공 PASSED
BoardMapperTest > 존재하지 않는 ID로 조회 시 null 반환 PASSED
BoardMapperTest > 게시글이 없으면 빈 목록 반환 PASSED
BoardMapperTest > insert에 전달된 게시글 필드값 정밀 검증 PASSED

5 tests completed, 5 passed
BUILD SUCCESSFUL
```

##### Refactor

Mock 기반 테스트로 인터페이스 계약을 검증했습니다. MyBatis XML과의 실제 SQL 매핑 검증이 필요하면 `@MybatisTest` 기반 통합테스트를 별도로 작성합니다.

### 4.5. 전체 커버리지 확인 + 리뷰

모든 계층의 TDD가 완료되면 전체 커버리지를 확인합니다.

```bash
# 전체 테스트 + 커버리지 보고서 + 기준 검증
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

```
> Task :test
> Task :jacocoTestReport
> Task :jacocoTestCoverageVerification

BUILD SUCCESSFUL in 15s
```

리뷰 에이전트로 품질 확인:

```bash
> tdd-review, BoardServiceTest 리뷰
```

```
## TDD 리뷰 결과: BoardServiceTest

### 종합 점수: 95/100 (A등급)

| 영역 | 점수 | 만점 |
|---|---|---|
| 구조 검증 | 29/30 | 30 |
| 품질 검증 | 38/40 | 40 |
| NH 규칙 | 28/30 | 30 |

### 안티패턴 탐지
안티패턴 없음
```

---

## 5. How — 실전 팁

### 5.1. 어떤 테스트를 먼저 작성할 것인가

```
1순위: Happy Case (정상 동작) — 기본 기능이 동작하는지 확인
2순위: Exception (예외 경로) — 비정상 상황에서 올바르게 실패하는지 확인
3순위: Edge Case (경계값) — null, 빈 값, 최대값 등
4순위: Mutation (변이 감지) — 코드 변경 시 테스트가 감지하는지
```

### 5.2. AI 에이전트 프롬프트 잘 쓰는 법

| 나쁜 예 | 좋은 예 |
|---|---|
| "테스트 만들어줘" | "BoardService 테스트 코드 생성" |
| "전부 다 테스트" | "service 패키지 전체 테스트 코드 생성" |
| (아무 설명 없이 클래스명만) | 요구사항과 함께 클래스명 제공 |

**에이전트에게 제공하면 좋은 정보**:

```
- 클래스명 또는 패키지명
- 주요 요구사항 (특히 신규 기능의 경우)
- 특별히 주의할 점 (예: "비밀번호 암호화 검증 필수")
```

### 5.3. 테스트가 설계를 이끄는 사례

TDD로 테스트를 먼저 작성하다 보면 다음과 같은 설계 개선이 자연스럽게 발생합니다:

```
테스트를 쓰려고 보니...                 → 설계가 자연스럽게 개선됨
──────────────────────────────────────────────────────────
"이 클래스 Mock이 5개나 필요하네?"      → 책임이 너무 많다. 분리하자.
"이 메서드를 테스트하려면 DB가 필요하네?" → 비즈니스 로직과 DB 접근을 분리하자.
"이 private 메서드를 직접 테스트하고 싶네" → public 인터페이스를 통해 검증하자.
"테스트 데이터 만드는 게 너무 복잡해"     → 객체 생성 로직을 단순화하자.
```

---

## 6. 트러블슈팅 & FAQ

### Q1. Red 단계에서 테스트가 이미 통과해요

```
원인: 테스트가 실질적인 검증을 하지 않고 있음
해결: assertThat이나 verify가 제대로 기대값을 검증하는지 확인
     Mock의 thenReturn 값과 기대값이 동일한지 확인
```

### Q2. 구현 코드 없이 테스트만 있으면 에이전트가 혼란스러워해요

```
원인: 신규 TDD에서는 소스 코드가 아직 없으므로 에이전트가 분석할 대상이 없음
해결: 요구사항을 상세히 제공하면 에이전트가 예상 인터페이스를 기반으로 테스트 생성
     또는 인터페이스(메서드 시그니처)만 먼저 정의한 후 에이전트에게 전달
```

### Q3. 어디까지 테스트해야 하나요?

```
원칙: 비즈니스 로직이 있는 곳은 반드시 테스트
     - Service: 필수 (핵심 로직)
     - Controller: 권장 (HTTP 레이어 검증)
     - Mapper: 선택 (Mock으로 인터페이스 검증, DB 연동은 통합테스트)
     - Util: 필수 (순수 함수, 테스트 가장 쉬움)

테스트하지 않아도 되는 것:
     - getter/setter만 있는 DTO
     - 설정 클래스 (Configuration)
     - Spring Boot 메인 클래스
```

### Q4. TDD가 오히려 더 느린 것 같아요

```
단기적으로는 느릴 수 있습니다. 하지만:
- 디버깅 시간이 줄어듭니다 (버그를 즉시 잡으니까)
- 회귀 테스트 시간이 제로입니다 (자동이니까)
- 리팩토링이 자유로워집니다 (안전망이 있으니까)
- 코드 리뷰가 빨라집니다 (테스트가 의도를 설명하니까)

장기적으로는 "테스트 없이 개발 → 버그 수정 → 테스트 추가" 보다 빠릅니다.
```

### Q5. 폐쇄망에서 의존성을 못 받아요

```
해결: 사내 Nexus/Artifactory에 필요한 의존성을 사전 등록
     필요 의존성 목록: docs/closed-network-dependencies.md 참조
```

---

## 7. Next — TDD 문화 확산

### 7.1. 팀 컨벤션 수립

TDD를 팀에 도입할 때 다음 컨벤션을 합의합니다.

```
[ ] 신규 코드는 반드시 테스트와 함께 커밋한다
[ ] PR(Pull Request)에 테스트 통과 여부를 포함한다
[ ] 커버리지 기준 (라인 80%, 분기 70%)을 합의한다
[ ] 테스트 코드도 코드 리뷰 대상에 포함한다
[ ] tdd-review 에이전트 점수 80점 이상을 합격 기준으로 한다
```

### 7.2. CI/CD 파이프라인에 테스트 게이트 추가

빌드 파이프라인에 테스트를 필수 단계로 추가합니다.

```
코드 커밋 → 빌드 → [테스트 실행] → [커버리지 검증] → 배포
                      │                  │
                      ├─ 실패 시 빌드 중단  ├─ 미달 시 빌드 중단
                      └─────────────────  └─────────────────
```

```bash
# CI 빌드 스크립트 예시
./gradlew clean test jacocoTestReport jacocoTestCoverageVerification
```

이 명령어가 실패하면 배포가 차단되므로, 테스트 없는 코드는 배포할 수 없습니다.

### 7.3. TDD 문화의 성숙도

```
Level 0: 테스트 없음
Level 1: 일부 코드에 테스트 존재 (AI 에이전트로 생성)     ← Phase 1
Level 2: 신규 코드는 TDD로 개발                          ← Phase 2
Level 3: CI/CD에 테스트 게이트 적용, PR 리뷰에 테스트 포함
Level 4: "테스트 없는 코드 = 미완성 코드" 라는 인식 정착   ← 최종 목표
```

> **"100% 커버리지가 목표가 아닙니다.**
> **테스트를 작성하는 것이 자연스러운 개발 습관이 되는 것이 목표입니다."**
