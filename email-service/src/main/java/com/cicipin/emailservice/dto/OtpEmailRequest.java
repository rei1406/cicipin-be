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
    @NotBlank(message = "{email.otp.name.required}")
    private String name;

    @NotBlank(message = "{email.otp.code.required}")
    private String otpCode;

    @Positive(message = "{email.otp.expiry.positive}")
    private int expiryMinutes = 5;
}
