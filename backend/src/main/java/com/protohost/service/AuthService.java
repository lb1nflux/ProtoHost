package com.protohost.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.protohost.dto.AuthRequest;
import com.protohost.dto.AuthResponse;
import com.protohost.entity.User;
import com.protohost.mapper.UserMapper;
import com.protohost.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(AuthRequest req) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())) != null) {
            throw new RuntimeException("邮箱已被注册");
        }
        User user = new User();
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userMapper.insert(user);
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }

    public AuthResponse login(AuthRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("邮箱或密码错误");
        }
        return new AuthResponse(jwtUtil.generateToken(user.getId(), user.getEmail()), user.getId(), user.getEmail());
    }
}
