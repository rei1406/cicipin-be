package com.cicipin.userservice.auth.dto;

import com.cicipin.userservice.common.model.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "{auth.register.username.required}")
    @Size(min = 3, max = 50, message = "{auth.register.username.size}")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "{auth.register.username.pattern}")
    private String username;

    @NotBlank(message = "{auth.register.name.required}")
    @Size(max = 100, message = "{auth.register.name.size}")
    private String name;

    @NotBlank(message = "{auth.register.email.required}")
    @Email(message = "{auth.register.email.invalid}")
    private String email;

    @NotBlank(message = "{auth.register.password.required}")
    @Size(min = 8, max = 255, message = "{auth.register.password.size}")
    private String password;

    @NotNull(message = "{auth.register.role.required}")
    private UserRole role;
}
