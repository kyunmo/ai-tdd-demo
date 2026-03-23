package nh.ai.tdd.demo.service;

import nh.ai.tdd.demo.domain.User;
import nh.ai.tdd.demo.dto.CreateUserRequest;
import nh.ai.tdd.demo.exception.DuplicateEmailException;
import nh.ai.tdd.demo.exception.UserNotFoundException;
import nh.ai.tdd.demo.mapper.UserMapper;
import nh.ai.tdd.demo.util.MaskingUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final RestTemplate restTemplate;

    public UserService(UserMapper userMapper, RestTemplate restTemplate) {
        this.userMapper = userMapper;
        this.restTemplate = restTemplate;
    }

    public User createUser(CreateUserRequest request) {
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new DuplicateEmailException(request.getEmail());
        }

        // 외부 API 호출하여 externalId 받아오기
        @SuppressWarnings("unchecked")
        Map<String, String> response = restTemplate.getForObject("http://localhost:8080/mock-api/external-system/user-info", Map.class);
        String externalId = response != null ? response.get("externalId") : null;

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setExternalId(externalId); // 받아온 externalId 설정

        userMapper.insert(user);
        return user;
    }

    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("유효하지 않은 사용자 ID: " + id);
        }

        User user = userMapper.findById(id);
        if (user == null) {
            throw new UserNotFoundException(id);
        }
        return user;
    }

    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    public User updateUser(Long id, CreateUserRequest request) {
        User user = getUserById(id);

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());

        userMapper.update(user);
        return user;
    }

    public void deleteUser(Long id) {
        getUserById(id);
        userMapper.deleteById(id);
    }

    public String getMaskedPhoneNumber(Long id) {
        User user = getUserById(id);
        return MaskingUtil.maskPhoneNumber(user.getPhoneNumber());
    }
}
