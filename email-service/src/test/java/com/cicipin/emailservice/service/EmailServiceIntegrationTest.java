package com.cicipin.emailservice.service;

import com.cicipin.emailservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration tests that send real emails via Gmail SMTP.
 *
 * These tests are only executed when the MAIL_USERNAME environment variable
 * is set (i.e. your SMTP credentials are configured). They are skipped
 * automatically when credentials are absent.
 *
 * To run inside the running dev container:
 *
 *   ./dev.sh up -d --build
 *   ./dev.sh exec email-service mvn test -Dsurefire.excludedGroups= -Dsurefire.groups=gmail
 *
 * Or via plain docker compose:
 *
 *   docker compose -f docker-compose.dev.yml exec email-service \
 *     mvn test -Dsurefire.excludedGroups= -Dsurefire.groups=gmail
 *
 * This test is tagged "gmail" and excluded from the default mvn test run.
 */
@SpringBootTest
@Tag("gmail")
@TestPropertySource(properties = {
    "server.port=${EMAIL_SERVICE_CONTAINER_PORT:8082}",
    "spring.mail.host=${MAIL_HOST:smtp.gmail.com}",
    "spring.mail.port=${MAIL_PORT:587}",
    "spring.mail.username=${MAIL_USERNAME:}",
    "spring.mail.password=${MAIL_PASSWORD:}",
    "spring.mail.from=${MAIL_FROM:}",
    "app.name=Cicipin"
})
@EnabledIfEnvironmentVariable(named = "MAIL_USERNAME", matches = ".+")
@DisplayName("EmailService Integration Tests (requires SMTP credentials)")
class EmailServiceIntegrationTest {

    private static final String TARGET_EMAIL = "eko.susilo@mhs.unsoed.ac.id";

    @Autowired
    private EmailService emailService;

    @BeforeEach
    void checkCredentials() {
        // Guard: skip gracefully if credentials slipped through the annotation check
        String username = System.getenv("MAIL_USERNAME");
        org.junit.jupiter.api.Assumptions.assumeTrue(
            username != null && !username.isBlank(),
            "Skipping integration test: MAIL_USERNAME not set"
        );
    }

    @Test
    @DisplayName("Sends OTP verification email to target address")
    void sendOtpVerificationEmail() {
        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko Susilo");
        req.setOtpCode("482910");
        req.setExpiryMinutes(5);

        emailService.send(req);
        // If no exception is thrown, the email was accepted by the SMTP server
    }

    @Test
    @DisplayName("Sends welcome email to target address")
    void sendWelcomeEmail() {
        WelcomeEmailRequest req = new WelcomeEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.WELCOME);
        req.setName("Eko Susilo");

        emailService.send(req);
    }

    @Test
    @DisplayName("Sends password reset email to target address")
    void sendPasswordResetEmail() {
        PasswordResetEmailRequest req = new PasswordResetEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.PASSWORD_RESET);
        req.setName("Eko Susilo");
        req.setResetCode("654321");
        req.setExpiryMinutes(15);

        emailService.send(req);
    }

    @Test
    @DisplayName("Sends generic email to target address")
    void sendGenericEmail() {
        GenericEmailRequest req = new GenericEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.GENERIC);
        req.setSubject("Cicipin — Test Notification");
        req.setBody("This is a test email from the Cicipin email service integration test.");

        emailService.send(req);
    }
}
