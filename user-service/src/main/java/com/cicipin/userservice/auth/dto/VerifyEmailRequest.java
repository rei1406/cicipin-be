package com.cicipin.userservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyEmailRequest {

    @NotBlank(message = "{auth.verify.email.required}")
    @Email(message = "{auth.register.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.verify.otp.required}")
    @Size(min = 6, max = 6, message = "{auth.verify.otp.size}")
    private String otp;
}
