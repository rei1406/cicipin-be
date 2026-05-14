package com.cicipin.emailservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request payload for sending a welcome email after successful registration.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class WelcomeEmailRequest extends SendEmailRequest {

    /** The recipient's display name shown in the email body. */
    @NotBlank(message = "Name must not be blank")
    private String name;
}
