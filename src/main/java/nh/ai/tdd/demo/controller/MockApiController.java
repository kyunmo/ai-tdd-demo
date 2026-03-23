package nh.ai.tdd.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * 외부 API 시스템을 시뮬레이션하는 Mock 컨트롤러.
 * 폐쇄망 환경에서 외부 API 연동을 테스트하기 위해 사용됩니다.
 */
@RestController
@RequestMapping("/mock-api/external-system")
public class MockApiController {

    /**
     * 외부 시스템의 사용자 정보 조회를 흉내 내는 엔드포인트.
     * 호출 시 가상의 외부 ID와 상태를 담은 JSON을 반환합니다.
     * @return 외부 시스템의 사용자 정보
     */
    @GetMapping("/user-info")
    public Map<String, String> getExternalUserInfo() {
        // 실제 외부 시스템이라면 이곳에서 복잡한 비즈니스 로직이 수행됩니다.
        // 여기서는 테스트를 위해 미리 정의된 값을 반환합니다.
        String externalId = "ext-" + UUID.randomUUID().toString().substring(0, 8);
        return Collections.singletonMap("externalId", externalId);
    }
}
