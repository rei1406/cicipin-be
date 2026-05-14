package com.cicipin.emailservice.dto;

/**
 * Supported email types. Each type maps to a Thymeleaf template
 * under {@code resources/templates/email/}.
 */
public enum EmailType {
    OTP_VERIFICATION,
    WELCOME,
    PASSWORD_RESET,
    GENERIC
}
