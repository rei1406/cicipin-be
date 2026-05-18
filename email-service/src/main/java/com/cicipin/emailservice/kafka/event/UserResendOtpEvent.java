package com.cicipin.emailservice.kafka.event;

public record UserResendOtpEvent(String email, String username, String name, String otpCode, int expiryMinutes) {
}
