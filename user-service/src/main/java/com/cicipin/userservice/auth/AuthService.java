package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.AuthResponse;
import com.cicipin.userservice.auth.dto.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    // TODO: login(LoginRequest request)
    // TODO: logout(String token)
    // TODO: refreshToken(String refreshToken)
    // TODO: verifyEmail(String token)
}
