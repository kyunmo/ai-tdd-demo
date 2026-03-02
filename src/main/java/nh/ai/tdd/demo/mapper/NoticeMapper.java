package nh.ai.tdd.demo.mapper;

import nh.ai.tdd.demo.domain.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    Notice findById(Long id);

    List<Notice> findAll();

    int insert(Notice notice);

    int update(Notice notice);

    int deleteById(Long id);

    void incrementViewCount(Long id);

    int count();
}
