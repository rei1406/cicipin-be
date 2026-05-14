package com.cicipin.emailservice.service;

import com.cicipin.emailservice.dto.*;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link EmailServiceImpl}.
 *
 * Uses Mockito to mock JavaMailSender and TemplateEngine — no SMTP connection,
 * no Spring context, runs instantly.
 *
 * To run inside the dev container:
 *
 *   ./dev.sh exec email-service mvn test -Dtest=EmailServiceImplTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Unit Tests")
class EmailServiceImplTest {

    private static final String TARGET_EMAIL = "eko.susilo@mhs.unsoed.ac.id";

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(emailService, "fromAddress", "no-reply@cicipin.com");
        ReflectionTestUtils.setField(emailService, "appName", "Cicipin");

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // -------------------------------------------------------------------------
    // OTP Verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("send() with OTP_VERIFICATION processes the correct template")
    void send_otpVerification_usesCorrectTemplate() {
        when(templateEngine.process(eq("email/otp-verification"), any(Context.class)))
            .thenReturn("<html>OTP: 123456</html>");

        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko Susilo");
        req.setOtpCode("123456");
        req.setExpiryMinutes(5);

        emailService.send(req);

        verify(templateEngine).process(eq("email/otp-verification"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("send() with OTP_VERIFICATION passes correct variables to template context")
    void send_otpVerification_passesCorrectContextVariables() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/otp-verification"), contextCaptor.capture()))
            .thenReturn("<html>OTP</html>");

        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko Susilo");
        req.setOtpCode("482910");
        req.setExpiryMinutes(5);

        emailService.send(req);

        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("name")).isEqualTo("Eko Susilo");
        assertThat(ctx.getVariable("otpCode")).isEqualTo("482910");
        assertThat(ctx.getVariable("expiryMinutes")).isEqualTo(5);
        assertThat(ctx.getVariable("appName")).isEqualTo("Cicipin");
    }

    @Test
    @DisplayName("send() with OTP_VERIFICATION uses default expiryMinutes of 5")
    void send_otpVerification_defaultExpiryMinutes() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/otp-verification"), contextCaptor.capture()))
            .thenReturn("<html>OTP</html>");

        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko");
        req.setOtpCode("999999");
        // expiryMinutes not set — should default to 5

        emailService.send(req);

        assertThat(contextCaptor.getValue().getVariable("expiryMinutes")).isEqualTo(5);
    }

    // -------------------------------------------------------------------------
    // Welcome
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("send() with WELCOME processes the correct template")
    void send_welcome_usesCorrectTemplate() {
        when(templateEngine.process(eq("email/welcome"), any(Context.class)))
            .thenReturn("<html>Welcome</html>");

        WelcomeEmailRequest req = new WelcomeEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.WELCOME);
        req.setName("Eko Susilo");

        emailService.send(req);

        verify(templateEngine).process(eq("email/welcome"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("send() with WELCOME passes correct variables to template context")
    void send_welcome_passesCorrectContextVariables() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/welcome"), contextCaptor.capture()))
            .thenReturn("<html>Welcome</html>");

        WelcomeEmailRequest req = new WelcomeEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.WELCOME);
        req.setName("Eko Susilo");

        emailService.send(req);

        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("name")).isEqualTo("Eko Susilo");
        assertThat(ctx.getVariable("appName")).isEqualTo("Cicipin");
    }

    // -------------------------------------------------------------------------
    // Password Reset
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("send() with PASSWORD_RESET processes the correct template")
    void send_passwordReset_usesCorrectTemplate() {
        when(templateEngine.process(eq("email/password-reset"), any(Context.class)))
            .thenReturn("<html>Reset</html>");

        PasswordResetEmailRequest req = new PasswordResetEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.PASSWORD_RESET);
        req.setName("Eko Susilo");
        req.setResetCode("654321");
        req.setExpiryMinutes(15);

        emailService.send(req);

        verify(templateEngine).process(eq("email/password-reset"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("send() with PASSWORD_RESET passes correct variables to template context")
    void send_passwordReset_passesCorrectContextVariables() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/password-reset"), contextCaptor.capture()))
            .thenReturn("<html>Reset</html>");

        PasswordResetEmailRequest req = new PasswordResetEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.PASSWORD_RESET);
        req.setName("Eko Susilo");
        req.setResetCode("654321");
        req.setExpiryMinutes(15);

        emailService.send(req);

        Context ctx = contextCaptor.getValue();
        assertThat(ctx.getVariable("name")).isEqualTo("Eko Susilo");
        assertThat(ctx.getVariable("resetCode")).isEqualTo("654321");
        assertThat(ctx.getVariable("expiryMinutes")).isEqualTo(15);
        assertThat(ctx.getVariable("appName")).isEqualTo("Cicipin");
    }

    @Test
    @DisplayName("send() with PASSWORD_RESET uses default expiryMinutes of 15")
    void send_passwordReset_defaultExpiryMinutes() {
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(eq("email/password-reset"), contextCaptor.capture()))
            .thenReturn("<html>Reset</html>");

        PasswordResetEmailRequest req = new PasswordResetEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.PASSWORD_RESET);
        req.setName("Eko");
        req.setResetCode("111111");
        // expiryMinutes not set — should default to 15

        emailService.send(req);

        assertThat(contextCaptor.getValue().getVariable("expiryMinutes")).isEqualTo(15);
    }

    // -------------------------------------------------------------------------
    // Generic
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("send() with GENERIC sends plain text without using template engine")
    void send_generic_doesNotUseTemplateEngine() {
        GenericEmailRequest req = new GenericEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.GENERIC);
        req.setSubject("Test notification");
        req.setBody("Hello from Cicipin!");

        emailService.send(req);

        verifyNoInteractions(templateEngine);
        verify(mailSender).send(mimeMessage);
    }

    // -------------------------------------------------------------------------
    // Error handling
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("send() wraps MessagingException in RuntimeException")
    void send_messagingException_wrappedAsRuntimeException() {
        when(templateEngine.process(eq("email/otp-verification"), any(Context.class)))
            .thenReturn("<html>OTP</html>");
        // Make createMimeMessage throw to simulate SMTP failure
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("SMTP connection refused"));

        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko");
        req.setOtpCode("000000");

        assertThatThrownBy(() -> emailService.send(req))
            .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("send() calls mailSender.send() exactly once per request")
    void send_callsMailSenderExactlyOnce() {
        when(templateEngine.process(anyString(), any(Context.class)))
            .thenReturn("<html>body</html>");

        OtpEmailRequest req = new OtpEmailRequest();
        req.setTo(TARGET_EMAIL);
        req.setType(EmailType.OTP_VERIFICATION);
        req.setName("Eko");
        req.setOtpCode("123456");

        emailService.send(req);

        verify(mailSender, times(1)).send(any(MimeMessage.class));
    }
}
