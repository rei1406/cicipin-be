package com.cicipin.userservice.kafka;

public record UserForgotPasswordEvent(String email, String username, String name, String otpCode, int expiryMinutes) {
}
