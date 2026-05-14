package com.cicipin.emailservice.service;

import com.cicipin.emailservice.dto.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Sends emails using JavaMailSender and Thymeleaf HTML templates.
 *
 * Template resolution:
 *   OTP_VERIFICATION  → templates/email/otp-verification.html
 *   WELCOME           → templates/email/welcome.html
 *   PASSWORD_RESET    → templates/email/password-reset.html
 *   GENERIC           → plain text (no template)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @Value("${app.name:Cicipin}")
    private String appName;

    @Override
    public void send(SendEmailRequest request) {
        switch (request.getType()) {
            case OTP_VERIFICATION  -> sendOtp((OtpEmailRequest) request);
            case WELCOME           -> sendWelcome((WelcomeEmailRequest) request);
            case PASSWORD_RESET    -> sendPasswordReset((PasswordResetEmailRequest) request);
            case GENERIC           -> sendGeneric((GenericEmailRequest) request);
        }
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private void sendOtp(OtpEmailRequest req) {
        Context ctx = new Context();
        ctx.setVariable("name", req.getName());
        ctx.setVariable("otpCode", req.getOtpCode());
        ctx.setVariable("expiryMinutes", req.getExpiryMinutes());
        ctx.setVariable("appName", appName);

        String html = templateEngine.process("email/otp-verification", ctx);
        sendHtml(req.getTo(), "Your " + appName + " verification code", html);
    }

    private void sendWelcome(WelcomeEmailRequest req) {
        Context ctx = new Context();
        ctx.setVariable("name", req.getName());
        ctx.setVariable("appName", appName);

        String html = templateEngine.process("email/welcome", ctx);
        sendHtml(req.getTo(), "Welcome to " + appName, html);
    }

    private void sendPasswordReset(PasswordResetEmailRequest req) {
        Context ctx = new Context();
        ctx.setVariable("name", req.getName());
        ctx.setVariable("resetCode", req.getResetCode());
        ctx.setVariable("expiryMinutes", req.getExpiryMinutes());
        ctx.setVariable("appName", appName);

        String html = templateEngine.process("email/password-reset", ctx);
        sendHtml(req.getTo(), "Reset your " + appName + " password", html);
    }

    private void sendGeneric(GenericEmailRequest req) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(req.getTo());
            helper.setSubject(req.getSubject());
            helper.setText(req.getBody(), false);
            mailSender.send(message);
            log.info("Generic email sent to {}", req.getTo());
        } catch (MessagingException e) {
            log.error("Failed to send generic email to {}: {}", req.getTo(), e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private void sendHtml(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
            log.info("HTML email [{}] sent to {}", subject, to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
