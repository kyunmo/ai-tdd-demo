package nh.ai.tdd.demo.mapper;

import nh.ai.tdd.demo.domain.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {

    User findById(Long id);

    User findByEmail(String email);

    List<User> findAll();

    int insert(User user);

    int update(User user);

    int deleteById(Long id);

    int count();
}
