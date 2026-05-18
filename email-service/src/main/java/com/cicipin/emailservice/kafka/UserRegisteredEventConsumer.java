package com.cicipin.emailservice.kafka;

import com.cicipin.emailservice.dto.EmailType;
import com.cicipin.emailservice.dto.OtpEmailRequest;
import com.cicipin.emailservice.kafka.event.UserRegisteredEvent;
import com.cicipin.emailservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserRegisteredEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-registered", groupId = "email-service")
    public void consume(String message) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);

            OtpEmailRequest request = new OtpEmailRequest();
            request.setTo(event.email());
            request.setType(EmailType.OTP_VERIFICATION);
            request.setName(event.name());
            request.setOtpCode(event.otpCode());
            request.setExpiryMinutes(event.expiryMinutes());

            emailService.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process user-registered event", e);
        }
    }
}
