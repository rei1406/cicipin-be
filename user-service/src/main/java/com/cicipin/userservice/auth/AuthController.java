package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.AuthResponse;
import com.cicipin.userservice.auth.dto.RegisterRequest;
import com.cicipin.userservice.common.versioning.ApiVersion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    // TODO: POST /login
    // TODO: POST /logout
    // TODO: POST /refresh-token
    // TODO: POST /verify-email
}
