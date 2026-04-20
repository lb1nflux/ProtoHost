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
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final VerificationCodeService verificationCodeService;
    private final EmailService emailService;

    public void sendRegisterCode(String email) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) != null) {
            throw new RuntimeException("该邮箱已被注册");
        }
        if (!verificationCodeService.canSend(email)) {
            throw new RuntimeException("验证码发送太频繁，请稍后再试");
        }
        String code = verificationCodeService.generateCode(email);
        emailService.sendSimpleMail(email, "ProtoHost 注册验证码", "您的验证码是：" + code + "，有效期为 10 分钟。");
    }

    public void sendResetCode(String email) {
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email)) == null) {
            throw new RuntimeException("该邮箱未注册");
        }
        if (!verificationCodeService.canSend(email)) {
            throw new RuntimeException("验证码发送太频繁，请稍后再试");
        }
        String code = verificationCodeService.generateCode(email);
        emailService.sendSimpleMail(email, "ProtoHost 找回密码验证码", "您的验证码是：" + code + "，有效期为 10 分钟。");
    }

    public void resetPassword(AuthRequest req) {
        if (!verificationCodeService.verifyCode(req.getEmail(), req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail()));
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        userMapper.updateById(user);
    }

    public AuthResponse register(AuthRequest req) {
        if (!verificationCodeService.verifyCode(req.getEmail(), req.getCode())) {
            throw new RuntimeException("验证码错误或已过期");
        }
        if (userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, req.getEmail())) != null) {
            throw new RuntimeException("该邮箱已被注册");
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
