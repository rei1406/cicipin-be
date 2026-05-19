package com.cicipin.userservice.auth.dto;

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
public class ResetPasswordRequest {

    @NotBlank(message = "{auth.reset.token.required}")
    private String token;

    @NotBlank(message = "{auth.reset.password.required}")
    @Size(min = 8, max = 255, message = "{auth.reset.password.size}")
    private String newPassword;
}
