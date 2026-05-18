package com.cicipin.userservice.auth.dto;

import com.cicipin.userservice.common.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private UUID id;
    private String username;
    private String name;
    private String email;
    private UserRole role;
}
