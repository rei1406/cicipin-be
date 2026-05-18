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

    private static final String RESEND_LIMIT_PREFIX = "resend:limit:";
    private static final int RESEND_MAX_ATTEMPTS = 5;
    private static final int RESEND_WINDOW_HOURS = 1;

    private static final String VERIFY_ATTEMPTS_PREFIX = "verify:attempts:";
    private static final String VERIFY_BLOCKED_PREFIX = "verify:blocked:";
    private static final int VERIFY_MAX_ATTEMPTS = 3;
    private static final int VERIFY_BLOCK_MINUTES = 5;

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

    @Override
    public boolean verifyOtp(String email, String otp) {
        String key = OTP_KEY_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        if (storedOtp == null) {
            return false;
        }
        boolean valid = storedOtp.equals(otp);
        if (valid) {
            redisTemplate.delete(key);
        }
        return valid;
    }

    @Override
    public void deleteOtp(String email) {
        String key = OTP_KEY_PREFIX + email;
        redisTemplate.delete(key);
    }

    @Override
    public boolean isResendAllowed(String email) {
        String key = RESEND_LIMIT_PREFIX + email;
        String count = redisTemplate.opsForValue().get(key);
        if (count == null) {
            return true;
        }
        return Integer.parseInt(count) < RESEND_MAX_ATTEMPTS;
    }

    @Override
    public void recordResendAttempt(String email) {
        String key = RESEND_LIMIT_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1) {
            redisTemplate.expire(key, RESEND_WINDOW_HOURS, TimeUnit.HOURS);
        }
    }

    @Override
    public boolean isVerifyAllowed(String email) {
        String blockedKey = VERIFY_BLOCKED_PREFIX + email;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockedKey))) {
            return false;
        }

        String attemptsKey = VERIFY_ATTEMPTS_PREFIX + email;
        String count = redisTemplate.opsForValue().get(attemptsKey);
        if (count == null) {
            return true;
        }

        int attempts = Integer.parseInt(count);
        if (attempts >= VERIFY_MAX_ATTEMPTS) {
            redisTemplate.delete(attemptsKey);
            return true;
        }

        return true;
    }

    @Override
    public void recordVerifyAttempt(String email) {
        String attemptsKey = VERIFY_ATTEMPTS_PREFIX + email;
        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        if (count != null && count >= VERIFY_MAX_ATTEMPTS) {
            String blockedKey = VERIFY_BLOCKED_PREFIX + email;
            redisTemplate.opsForValue().set(blockedKey, "1", VERIFY_BLOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    @Override
    public void resetVerifyAttempts(String email) {
        redisTemplate.delete(VERIFY_ATTEMPTS_PREFIX + email);
        redisTemplate.delete(VERIFY_BLOCKED_PREFIX + email);
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
