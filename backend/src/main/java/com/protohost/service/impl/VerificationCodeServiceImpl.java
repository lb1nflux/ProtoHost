package com.protohost.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.protohost.service.VerificationCodeService;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class VerificationCodeServiceImpl implements VerificationCodeService {

    // 验证码缓存：10分钟有效
    private final Cache<String, String> codeCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    // 发送限制缓存：60秒内不允许重复发送
    private final Cache<String, Long> rateLimitCache = Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build();

    private final Random random = new Random();

    @Override
    public String generateCode(String email) {
        String code = String.format("%06d", random.nextInt(1000000));
        codeCache.put(email, code);
        rateLimitCache.put(email, System.currentTimeMillis());
        return code;
    }

    @Override
    public boolean verifyCode(String email, String code) {
        String cachedCode = codeCache.getIfPresent(email);
        if (cachedCode != null && cachedCode.equals(code)) {
            // 验证通过后清除缓存，防止重复使用
            codeCache.invalidate(email);
            return true;
        }
        return false;
    }

    @Override
    public boolean canSend(String email) {
        return rateLimitCache.getIfPresent(email) == null;
    }
}
