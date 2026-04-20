package com.protohost.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.protohost.dto.AuthRequest;
import com.protohost.dto.AuthResponse;
import com.protohost.entity.User;
import com.protohost.mapper.UserMapper;
import com.protohost.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;
    @Mock
    private VerificationCodeService codeService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");
        authRequest.setCode("123456");
    }

    @Test
    @DisplayName("注册成功 - 验证码正确且邮箱未占用")
    void register_Success() {
        // Arrange
        when(codeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_password");
        when(jwtUtil.generateToken(anyLong(), anyString())).thenReturn("test_token");

        // Act
        AuthResponse response = authService.register(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("test_token", response.getToken());
        verify(userMapper, times(1)).insert(any(User.class));
    }

    @Test
    @DisplayName("注册失败 - 验证码错误或过期")
    void register_WrongCode() {
        // Arrange
        when(codeService.verifyCode(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(authRequest));
        assertEquals("验证码错误或已过期", exception.getMessage());
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("发送验证码 - 频繁发送拦截")
    void sendRegisterCode_RateLimit() {
        // Arrange
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(codeService.canSend(anyString())).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.sendRegisterCode("test@example.com"));
        assertEquals("发送频繁，请稍后再试", exception.getMessage());
        verify(emailService, never()).sendHtmlMail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("重置密码成功 - 验证码正确")
    void resetPassword_Success() {
        // Arrange
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(codeService.verifyCode(anyString(), anyString())).thenReturn(true);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(mockUser);
        when(passwordEncoder.encode(anyString())).thenReturn("new_hashed_password");

        // Act
        authService.resetPassword(authRequest);

        // Assert
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("重置密码失败 - 验证码错误")
    void resetPassword_WrongCode() {
        // Arrange
        when(codeService.verifyCode(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> authService.resetPassword(authRequest));
        verify(userMapper, never()).updateById(any(User.class));
    }
}
