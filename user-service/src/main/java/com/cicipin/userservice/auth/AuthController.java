package com.cicipin.userservice.auth;

import com.cicipin.userservice.common.versioning.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // TODO: POST /register
    // TODO: POST /login
    // TODO: POST /logout
    // TODO: POST /refresh-token
    // TODO: POST /verify-email
}
