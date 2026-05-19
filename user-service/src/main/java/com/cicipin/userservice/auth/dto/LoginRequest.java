package com.cicipin.userservice.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "{auth.login.username.required}")
    private String usernameOrEmail;

    @NotBlank(message = "{auth.login.password.required}")
    private String password;
}
