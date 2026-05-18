package com.cicipin.userservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private static final String TOPIC_USER_REGISTERED = "user-registered";
    private static final String TOPIC_USER_RESEND_OTP = "user-resend-otp";
    private static final String TOPIC_USER_FORGOT_PASSWORD = "user-forgot-password";
    private static final String TOPIC_USER_VERIFIED = "user-verified";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendUserRegistered(UserRegisteredEvent event) {
        sendEvent(TOPIC_USER_REGISTERED, event.email(), event, "user registered");
    }

    public void sendUserResendOtp(UserResendOtpEvent event) {
        sendEvent(TOPIC_USER_RESEND_OTP, event.email(), event, "user resend otp");
    }

    public void sendUserForgotPassword(UserForgotPasswordEvent event) {
        sendEvent(TOPIC_USER_FORGOT_PASSWORD, event.email(), event, "user forgot password");
    }

    public void sendUserVerified(UserVerifiedEvent event) {
        sendEvent(TOPIC_USER_VERIFIED, event.email(), event, "user verified");
    }

    private void sendEvent(String topic, String key, Object event, String logLabel) {
        try {
            String json = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, json);
            log.info("{} event sent to Kafka: {}", logLabel, key);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize {} event: {}", logLabel, e.getMessage());
        }
    }
}
