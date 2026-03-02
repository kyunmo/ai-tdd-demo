package nh.ai.tdd.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nh.ai.tdd.demo.domain.User;
import nh.ai.tdd.demo.dto.CreateUserRequest;
import nh.ai.tdd.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(name = "사용자 API", description = "사용자 CRUD 및 개인정보 마스킹 API")
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "전체 사용자 조회", description = "등록된 모든 사용자를 조회합니다")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "사용자 단건 조회", description = "ID로 사용자를 조회합니다")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "사용자 등록", description = "새로운 사용자를 등록합니다")
    @ApiResponse(responseCode = "201", description = "등록 성공")
    @ApiResponse(responseCode = "409", description = "이메일 중복")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @Operation(summary = "사용자 수정", description = "기존 사용자 정보를 수정합니다")
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @Parameter(description = "사용자 ID") @PathVariable Long id,
            @RequestBody CreateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    @Operation(summary = "사용자 삭제", description = "사용자를 삭제합니다")
    @ApiResponse(responseCode = "204", description = "삭제 성공")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "마스킹된 전화번호 조회", description = "사용자의 전화번호를 마스킹하여 반환합니다")
    @GetMapping("/{id}/masked-phone")
    public ResponseEntity<Map<String, String>> getMaskedPhone(
            @Parameter(description = "사용자 ID") @PathVariable Long id) {
        String masked = userService.getMaskedPhoneNumber(id);
        return ResponseEntity.ok(Collections.singletonMap("maskedPhoneNumber", masked));
    }

}
