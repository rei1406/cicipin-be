package com.cicipin.emailservice.kafka;

import com.cicipin.emailservice.dto.EmailType;
import com.cicipin.emailservice.dto.PasswordResetEmailRequest;
import com.cicipin.emailservice.kafka.event.UserForgotPasswordEvent;
import com.cicipin.emailservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserForgotPasswordEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-forgot-password", groupId = "email-service")
    public void consume(String message) {
        try {
            UserForgotPasswordEvent event = objectMapper.readValue(message, UserForgotPasswordEvent.class);

            PasswordResetEmailRequest request = new PasswordResetEmailRequest();
            request.setTo(event.email());
            request.setType(EmailType.PASSWORD_RESET);
            request.setName(event.name());
            request.setResetCode(event.otpCode());
            request.setExpiryMinutes(event.expiryMinutes());

            emailService.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process user-forgot-password event", e);
        }
    }
}
