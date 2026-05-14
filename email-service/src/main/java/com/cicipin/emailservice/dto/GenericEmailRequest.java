package com.cicipin.emailservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Request payload for sending a plain-text / generic email.
 * Useful for ad-hoc notifications that don't need a dedicated template.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class GenericEmailRequest extends SendEmailRequest {

    @NotBlank(message = "Subject must not be blank")
    private String subject;

    @NotBlank(message = "Body must not be blank")
    private String body;
}
