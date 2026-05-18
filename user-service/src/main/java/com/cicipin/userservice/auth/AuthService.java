package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse verifyEmail(VerifyEmailRequest request);

    AuthResponse resendOtp(ResendOtpRequest request);

    AuthResponse forgotPassword(ForgotPasswordRequest request);

    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

    AuthResponse resetPassword(ResetPasswordRequest request);
}
