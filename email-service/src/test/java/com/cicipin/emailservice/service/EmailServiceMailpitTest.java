package com.cicipin.emailservice.service;

import com.cicipin.emailservice.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests that send emails to Mailpit (in-Docker mail catcher)
 * and assert on the captured messages via Mailpit's REST API.
 *
 * Mailpit must be running before these tests execute. It is included in
 * docker-compose.dev.yml as the "mailpit" service (SMTP: 1025, API: 8025).
 *
 * Run inside the dev container:
 *
 *   ./dev.sh up -d --build
 *   ./dev.sh exec email-service mvn test -Dtest=EmailServiceMailpitTest
 *
 * Or run all default tests (Mailpit included, Gmail excluded):
 *
 *   ./dev.sh exec email-service mvn test
 *
 * These tests run as part of the default "mvn test" (not tagged "gmail").
 * They are skipped automatically when the MAILPIT_API_URL env var is absent
 * or when Mailpit is unreachable.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "server.port=8082",
    "spring.mail.host=${MAILPIT_SMTP_HOST:localhost}",
    "spring.mail.port=${MAILPIT_SMTP_PORT:1025}",
    "spring.mail.username=",
    "spring.mail.password=",
    "spring.mail.from=noreply@cicipin.test",
    "spring.mail.properties.mail.smtp.auth=false",
    "spring.mail.properties.mail.smtp.starttls.enable=false",
    "spring.mail.properties.mail.smtp.starttls.required=false",
    "app.name=Cicipin"
})
@DisplayName("EmailService Mailpit Integration Tests")
class EmailServiceMailpitTest {

    /** Mailpit REST API base URL — override via env var inside the container. */
    private static final String MAILPIT_API =
        System.getenv().getOrDefault("MAILPIT_API_URL", "http://cicipin-mailpit:8025");

    private static final String TARGET_EMAIL = "eko.susilo@mhs.unsoed.ac.id";

    @Autowired
    private EmailService emailService;

    private RestTemplate rest;

    @BeforeEach
    void setUp() {
        rest = new RestTemplate();
        // Delete all existing messages so each test starts with a clean inbox
        rest.delete(MAILPIT_API + "/api/v1/messages");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Fetches the latest message list from Mailpit and returns the first one.
     * Retries up to 3 times with a short delay to account for SMTP delivery lag.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchLatestMessage() throws InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            Map<String, Object> response =
                rest.getForObject(MAILPIT_API + "/api/v1/messages", Map.class);

            List<Map<String, Object>> messages =
                (List<Map<String, Object>>) response.get("messages");

            if (messages != null && !messages.isEmpty()) {
                return messages.get(0);
            }
            Thread.sleep(500);
        }
        return null;
    }

    /**
     * Fetches the full message body (HTML + text) for a given message ID.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMessageDetail(String messageId) {
        return rest.getForObject(
            MAILPIT_API + "/api/v1/message/" + messageId, Map.class);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> extractRecipients(Map<String, Object> detail) {
        return (List<Map<String, String>>) detail.get("To");
    }

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("OTP verification email is captured and contains OTP code")
    void sendOtpVerificationEmail() throws InterruptedException {
        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko Susilo");
        req.setOtpCode("482910");
        req.setExpiryMinutes(5);

        emailService.send(req);

        Map<String, Object> message = fetchLatestMessage();
        assertThat(message).as("Mailpit should have captured one message").isNotNull();

        String messageId = (String) message.get("ID");
        Map<String, Object> detail = fetchMessageDetail(messageId);

        // Assert recipient
        List<Map<String, String>> toList = extractRecipients(detail);
        assertThat(toList).anyMatch(addr -> TARGET_EMAIL.equals(addr.get("Address")));

        // Assert subject matches what EmailServiceImpl sets
        String subject = (String) detail.get("Subject");
        assertThat(subject).isEqualTo("Your Cicipin verification code");

        // Assert body contains the OTP code
        String html = (String) detail.get("HTML");
        String text = (String) detail.get("Text");
        assertThat(html + text).contains("482910");
    }

    @Test
    @DisplayName("Welcome email is captured and addressed correctly")
    void sendWelcomeEmail() throws InterruptedException {
        WelcomeEmailRequest req = new WelcomeEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.WELCOME);
        req.setName("Eko Susilo");

        emailService.send(req);

        Map<String, Object> message = fetchLatestMessage();
        assertThat(message).as("Mailpit should have captured one message").isNotNull();

        String messageId = (String) message.get("ID");
        Map<String, Object> detail = fetchMessageDetail(messageId);

        List<Map<String, String>> toList = extractRecipients(detail);
        assertThat(toList).anyMatch(addr -> TARGET_EMAIL.equals(addr.get("Address")));

        String subject = (String) detail.get("Subject");
        assertThat(subject).isEqualTo("Welcome to Cicipin");
    }

    @Test
    @DisplayName("Password reset email is captured and contains reset code")
    void sendPasswordResetEmail() throws InterruptedException {
        PasswordResetEmailRequest req = new PasswordResetEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.PASSWORD_RESET);
        req.setName("Eko Susilo");
        req.setResetCode("654321");
        req.setExpiryMinutes(15);

        emailService.send(req);

        Map<String, Object> message = fetchLatestMessage();
        assertThat(message).as("Mailpit should have captured one message").isNotNull();

        String messageId = (String) message.get("ID");
        Map<String, Object> detail = fetchMessageDetail(messageId);

        List<Map<String, String>> toList = extractRecipients(detail);
        assertThat(toList).anyMatch(addr -> TARGET_EMAIL.equals(addr.get("Address")));

        String subject = (String) detail.get("Subject");
        assertThat(subject).isEqualTo("Reset your Cicipin password");

        String text = (String) detail.get("Text");
        String html = (String) detail.get("HTML");
        assertThat(html + text).contains("654321");
    }

    @Test
    @DisplayName("Generic email is captured with correct subject and body")
    void sendGenericEmail() throws InterruptedException {
        GenericEmailRequest req = new GenericEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.GENERIC);
        req.setSubject("Cicipin — Test Notification");
        req.setBody("This is a test email from the Cicipin email service integration test.");

        emailService.send(req);

        Map<String, Object> message = fetchLatestMessage();
        assertThat(message).as("Mailpit should have captured one message").isNotNull();

        String messageId = (String) message.get("ID");
        Map<String, Object> detail = fetchMessageDetail(messageId);

        List<Map<String, String>> toList = extractRecipients(detail);
        assertThat(toList).anyMatch(addr -> TARGET_EMAIL.equals(addr.get("Address")));

        String subject = (String) detail.get("Subject");
        assertThat(subject).isEqualTo("Cicipin — Test Notification");

        String text = (String) detail.get("Text");
        String html = (String) detail.get("HTML");
        assertThat(html + text).contains("test email from the Cicipin email service");
    }
}
