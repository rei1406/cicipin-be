package com.cicipin.emailservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request payload for sending an OTP verification email.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OtpEmailRequest extends SendEmailRequest {

    /** The recipient's display name shown in the email body. */
    @NotBlank(message = "Name must not be blank")
    private String name;

    /** The 6-digit OTP code to include in the email. */
    @NotBlank(message = "OTP code must not be blank")
    private String otpCode;

    /**
     * How many minutes the OTP is valid for.
     * Shown in the email so the user knows the expiry window.
     */
    @Positive(message = "Expiry minutes must be a positive number")
    private int expiryMinutes = 5;
}
