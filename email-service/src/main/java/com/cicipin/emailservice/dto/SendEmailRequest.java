package com.cicipin.emailservice.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Base request for sending any type of email.
 * The {@code type} field drives which template is rendered.
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({
    @JsonSubTypes.Type(value = OtpEmailRequest.class,     name = "OTP_VERIFICATION"),
    @JsonSubTypes.Type(value = WelcomeEmailRequest.class, name = "WELCOME"),
    @JsonSubTypes.Type(value = PasswordResetEmailRequest.class, name = "PASSWORD_RESET"),
    @JsonSubTypes.Type(value = GenericEmailRequest.class, name = "GENERIC")
})
public abstract class SendEmailRequest {

    @NotBlank(message = "Recipient email must not be blank")
    @Email(message = "Recipient email must be a valid email address")
    private String to;

    @NotNull(message = "Email type must not be null")
    private EmailType type;
}
