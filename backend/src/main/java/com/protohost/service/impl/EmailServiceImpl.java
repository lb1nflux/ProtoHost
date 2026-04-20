package com.protohost.service.impl;

import com.protohost.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        send(to, subject, content, false);
    }

    @Override
    public void sendHtmlMail(String to, String subject, String content) {
        send(to, subject, content, true);
    }

    private void send(String to, String subject, String content, boolean isHtml) {
        if ("smtp.example.com".equals(host)) {
            log.info("【模拟发送】邮件 -> TO: {}, SUBJECT: {}, CONTENT: {}", to, subject, content);
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", String.valueOf(port));
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "false");
        
        // 超时设置调大一点
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        // 对齐 swaks 的关键配置
        if (port == 465) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", "465");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }
        
        // 重要：显式设置 localhost 避免被阿里云认为是滥用
        props.put("mail.smtp.localhost", "flowed.me");
        props.put("mail.smtp.ssl.trust", "*");
        // 允许自动协商 TLS 版本
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");

        Session session = Session.getInstance(props);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            
            if (isHtml) {
                message.setContent(content, "text/html;charset=UTF-8");
            } else {
                message.setText(content);
            }

            log.info("正在尝试连接 SMTP 服务器 {}:{} (用户: {})...", host, port, username);
            try (Transport transport = session.getTransport("smtp")) {
                transport.connect(host, username, password);
                transport.sendMessage(message, message.getAllRecipients());
            }
            
            log.info("邮件已成功发送至 {}", to);
        } catch (MessagingException e) {
            log.error("SMTP 发送失败，异常堆栈：", e);
            throw new RuntimeException("邮件服务异常：" + e.getMessage());
        }
    }
}
