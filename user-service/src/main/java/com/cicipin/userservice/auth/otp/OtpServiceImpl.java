package com.cicipin.userservice.auth.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private static final String OTP_KEY_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final String DIGITS = "0123456789";

    private final StringRedisTemplate redisTemplate;

    @Value("${app.otp.expiry-minutes:15}")
    private int expiryMinutes;

    @Override
    public String generateOtp(String email) {
        String otp = generateRandomOtp();
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.opsForValue().set(key, otp, expiryMinutes, TimeUnit.MINUTES);
        return otp;
    }

    private String generateRandomOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        return otp.toString();
    }
}
