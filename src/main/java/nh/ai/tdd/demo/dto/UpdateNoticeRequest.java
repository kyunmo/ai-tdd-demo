package nh.ai.tdd.demo.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateNoticeRequest {

    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다")
    private String title;

    @NotBlank(message = "내용은 필수입니다")
    private String content;

    public UpdateNoticeRequest() {
    }

    public UpdateNoticeRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
