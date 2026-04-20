package com.protohost.service;

public interface VerificationCodeService {
    /**
     * 为指定邮箱生成并存储 6 位验证码
     * @param email 邮箱地址
     * @return 生成的验证码
     */
    String generateCode(String email);

    /**
     * 校验验证码是否正确
     * @param email 邮箱地址
     * @param code 用户输入的验证码
     * @return 是否校验通过
     */
    boolean verifyCode(String email, String code);

    /**
     * 检查是否允许再次发送验证码（用于 60s 发送限制）
     * @param email 邮箱地址
     * @return 是否允许发送
     */
    boolean canSend(String email);
}
