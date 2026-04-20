package com.protohost.controller;

import com.protohost.dto.AuthRequest;
import com.protohost.dto.AuthResponse;
import com.protohost.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-register-code")
    public ResponseEntity<String> sendRegisterCode(@RequestParam String email) {
        authService.sendRegisterCode(email);
        return ResponseEntity.ok("验证码已发送");
    }

    @PostMapping("/send-reset-code")
    public ResponseEntity<String> sendResetCode(@RequestParam String email) {
        authService.sendResetCode(email);
        return ResponseEntity.ok("验证码已发送");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody AuthRequest req) {
        authService.resetPassword(req);
        return ResponseEntity.ok("密码已重置");
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }
}
