package com.cicipin.userservice.auth.otp;

public interface OtpService {

    String generateOtp(String email);

    boolean verifyOtp(String email, String otp);

    void deleteOtp(String email);

    boolean isResendAllowed(String email);

    void recordResendAttempt(String email);

    boolean isVerifyAllowed(String email);

    void recordVerifyAttempt(String email);

    void resetVerifyAttempts(String email);
}
