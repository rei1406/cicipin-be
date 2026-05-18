package com.cicipin.emailservice.kafka;

import com.cicipin.emailservice.dto.EmailType;
import com.cicipin.emailservice.dto.OtpEmailRequest;
import com.cicipin.emailservice.kafka.event.UserResendOtpEvent;
import com.cicipin.emailservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResendOtpEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-resend-otp", groupId = "email-service")
    public void consume(String message) {
        try {
            UserResendOtpEvent event = objectMapper.readValue(message, UserResendOtpEvent.class);

            OtpEmailRequest request = new OtpEmailRequest();
            request.setTo(event.email());
            request.setType(EmailType.OTP_VERIFICATION);
            request.setName(event.name());
            request.setOtpCode(event.otpCode());
            request.setExpiryMinutes(event.expiryMinutes());

            emailService.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process user-resend-otp event", e);
        }
    }
}
