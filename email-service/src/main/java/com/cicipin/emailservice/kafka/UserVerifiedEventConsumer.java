package com.cicipin.emailservice.kafka;

import com.cicipin.emailservice.dto.EmailType;
import com.cicipin.emailservice.dto.WelcomeEmailRequest;
import com.cicipin.emailservice.kafka.event.UserVerifiedEvent;
import com.cicipin.emailservice.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserVerifiedEventConsumer {

    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "user-verified", groupId = "email-service")
    public void consume(String message) {
        try {
            UserVerifiedEvent event = objectMapper.readValue(message, UserVerifiedEvent.class);

            WelcomeEmailRequest request = new WelcomeEmailRequest();
            request.setTo(event.email());
            request.setType(EmailType.WELCOME);
            request.setName(event.name());

            emailService.send(request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process user-verified event", e);
        }
    }
}
