package com.cicipin.emailservice.kafka.event;

public record UserForgotPasswordEvent(String email, String username, String name, String otpCode, int expiryMinutes) {
}
