package nh.ai.tdd.demo.controller;

import nh.ai.tdd.demo.domain.Notice;
import nh.ai.tdd.demo.domain.User;
import nh.ai.tdd.demo.service.NoticeService;
import nh.ai.tdd.demo.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * Thymeleaf 템플릿을 사용하여 웹 페이지를 렌더링하는 컨트롤러.
 */
@Controller
@RequestMapping("/")
public class WebController {

    private final UserService userService;
    private final NoticeService noticeService;

    public WebController(UserService userService, NoticeService noticeService) {
        this.userService = userService;
        this.noticeService = noticeService;
    }

    /**
     * 메인 페이지.
     * @return 메인 페이지 템플릿 이름
     */
    @GetMapping
    public String index() {
        return "index";
    }

    /**
     * 사용자 목록 페이지.
     * @param model 뷰에 전달할 모델
     * @return 사용자 목록 페이지 템플릿 이름
     */
    @GetMapping("/users")
    public String userList(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users/list";
    }

    /**
     * 공지사항 목록 페이지.
     * @param model 뷰에 전달할 모델
     * @return 공지사항 목록 페이지 템플릿 이름
     */
    @GetMapping("/notices")
    public String noticeList(Model model) {
        List<Notice> notices = noticeService.getAllNotices();
        model.addAttribute("notices", notices);
        return "notices/list";
    }
    
    /**
     * 공지사항 상세 페이지.
     * @param id 공지사항 ID
     * @param model 뷰에 전달할 모델
     * @return 공지사항 상세 페이지 템플릿 이름
     */
    @GetMapping("/notices/{id}")
    public String noticeDetail(@PathVariable Long id, Model model) {
        Notice notice = noticeService.getNoticeById(id);
        model.addAttribute("notice", notice);
        return "notices/detail";
    }
}
