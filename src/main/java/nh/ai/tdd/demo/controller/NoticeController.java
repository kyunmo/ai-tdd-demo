package nh.ai.tdd.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nh.ai.tdd.demo.domain.Notice;
import nh.ai.tdd.demo.dto.CreateNoticeRequest;
import nh.ai.tdd.demo.dto.UpdateNoticeRequest;
import nh.ai.tdd.demo.service.NoticeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "공지사항 API", description = "공지사항 CRUD API")
@RestController
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "전체 공지사항 조회", description = "등록된 모든 공지사항을 조회합니다")
    @GetMapping
    public ResponseEntity<List<Notice>> getAllNotices() {
        return ResponseEntity.ok(noticeService.getAllNotices());
    }

    @Operation(summary = "공지사항 단건 조회", description = "ID로 공지사항을 조회합니다 (조회수 자동 증가)")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    @GetMapping("/{id}")
    public ResponseEntity<Notice> getNoticeById(
            @Parameter(description = "공지사항 ID") @PathVariable Long id) {
        return ResponseEntity.ok(noticeService.getNoticeById(id));
    }

    @Operation(summary = "공지사항 등록", description = "새로운 공지사항을 등록합니다")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @PostMapping
    public ResponseEntity<Notice> createNotice(@RequestBody CreateNoticeRequest request) {
        Notice notice = noticeService.createNotice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(notice);
    }

    @Operation(summary = "공지사항 수정", description = "기존 공지사항을 수정합니다")
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    @PutMapping("/{id}")
    public ResponseEntity<Notice> updateNotice(
            @Parameter(description = "공지사항 ID") @PathVariable Long id,
            @RequestBody UpdateNoticeRequest request) {
        return ResponseEntity.ok(noticeService.updateNotice(id, request));
    }

    @Operation(summary = "공지사항 삭제", description = "공지사항을 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotice(
            @Parameter(description = "공지사항 ID") @PathVariable Long id) {
        noticeService.deleteNotice(id);
        return ResponseEntity.noContent().build();
    }
}
