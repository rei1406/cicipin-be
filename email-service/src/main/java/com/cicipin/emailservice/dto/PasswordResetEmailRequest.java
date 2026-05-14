package com.cicipin.emailservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request payload for sending a password-reset OTP email.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PasswordResetEmailRequest extends SendEmailRequest {

    /** The recipient's display name shown in the email body. */
    @NotBlank(message = "Name must not be blank")
    private String name;

    /** The OTP / reset code to include in the email. */
    @NotBlank(message = "Reset code must not be blank")
    private String resetCode;

    /**
     * How many minutes the reset code is valid for.
     */
    @Positive(message = "Expiry minutes must be a positive number")
    private int expiryMinutes = 15;
}
