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
    @NotBlank(message = "{email.reset.name.required}")
    private String name;

    @NotBlank(message = "{email.reset.code.required}")
    private String resetCode;

    @Positive(message = "{email.reset.expiry.positive}")
    private int expiryMinutes = 15;
}
