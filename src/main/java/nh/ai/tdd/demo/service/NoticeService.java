package nh.ai.tdd.demo.service;

import nh.ai.tdd.demo.domain.Notice;
import nh.ai.tdd.demo.dto.CreateNoticeRequest;
import nh.ai.tdd.demo.dto.UpdateNoticeRequest;
import nh.ai.tdd.demo.exception.NoticeNotFoundException;
import nh.ai.tdd.demo.mapper.NoticeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public NoticeService(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    public Notice getNoticeById(Long id) {
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new NoticeNotFoundException(id);
        }
        noticeMapper.incrementViewCount(id);
        return notice;
    }

    public List<Notice> getAllNotices() {
        return noticeMapper.findAll();
    }

    public Notice createNotice(CreateNoticeRequest request) {
        Notice notice = new Notice();
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setAuthor(request.getAuthor());

        noticeMapper.insert(notice);
        return notice;
    }

    public Notice updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new NoticeNotFoundException(id);
        }

        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());

        noticeMapper.update(notice);
        return notice;
    }

    public void deleteNotice(Long id) {
        Notice notice = noticeMapper.findById(id);
        if (notice == null) {
            throw new NoticeNotFoundException(id);
        }
        noticeMapper.deleteById(id);
    }
}
