package com.cicipin.emailservice.kafka.event;

public record UserRegisteredEvent(String email, String username, String name, String otpCode, int expiryMinutes) {
}
