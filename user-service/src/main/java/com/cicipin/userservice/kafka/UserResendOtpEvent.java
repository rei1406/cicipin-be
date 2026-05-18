package com.cicipin.userservice.kafka;

public record UserResendOtpEvent(String email, String username, String name, String otpCode, int expiryMinutes) {
}
