package com.cicipin.userservice.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {

    @NotBlank(message = "{auth.verifyotp.email.required}")
    @Email(message = "{auth.register.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.verifyotp.otp.required}")
    private String otp;
}
