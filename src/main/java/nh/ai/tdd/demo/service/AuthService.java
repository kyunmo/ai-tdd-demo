package nh.ai.tdd.demo.service;

import nh.ai.tdd.demo.domain.User;
import nh.ai.tdd.demo.dto.LoginRequest;
import nh.ai.tdd.demo.dto.LoginResponse;
import nh.ai.tdd.demo.dto.SignupRequest;
import nh.ai.tdd.demo.dto.SignupResponse;
import nh.ai.tdd.demo.exception.DuplicateEmailException;
import nh.ai.tdd.demo.exception.InvalidCredentialsException;
import nh.ai.tdd.demo.mapper.UserMapper;
import nh.ai.tdd.demo.security.JwtTokenProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.getEmail());
        if (user == null) {
            throw new InvalidCredentialsException();
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getEmail(), user.getName());
    }

    public SignupResponse signup(SignupRequest request) {
        User existing = userMapper.findByEmail(request.getEmail());
        if (existing != null) {
            throw new DuplicateEmailException(request.getEmail());
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole("ROLE_USER");

        userMapper.insert(user);
        return new SignupResponse(user.getId(), user.getEmail(), user.getName());
    }
}
