package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.*;
import com.cicipin.userservice.common.dto.ApiResponse;
import com.cicipin.userservice.common.versioning.ApiVersion;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@ApiVersion(1)
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse data = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, data, "Registration successful. Please check your email for verification."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse data = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "Login successful."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        AuthResponse data = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "Email verified successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> resendOtp(@Valid @RequestBody ResendOtpRequest request) {
        AuthResponse data = authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "OTP resent successfully. Please check your email."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<AuthResponse>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        AuthResponse data = authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "Password reset OTP sent. Please check your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<VerifyOtpResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        VerifyOtpResponse data = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "OTP verified. Use the token to reset your password."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<AuthResponse>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        AuthResponse data = authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, data, "Password reset successful."));
    }
}
