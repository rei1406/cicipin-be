package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.*;
import com.cicipin.userservice.auth.otp.OtpService;
import com.cicipin.userservice.common.exception.BadRequestException;
import com.cicipin.userservice.common.exception.DuplicateResourceException;
import com.cicipin.userservice.common.exception.ResourceNotFoundException;
import com.cicipin.userservice.common.exception.ForbiddenException;
import com.cicipin.userservice.common.exception.UnauthorizedException;
import com.cicipin.userservice.common.model.User;
import com.cicipin.userservice.common.model.UserRole;
import com.cicipin.userservice.kafka.*;
import com.cicipin.userservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final UserEventProducer userEventProducer;
    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    private static final String RESET_TOKEN_PREFIX = "reset:token:";
    private static final int RESET_TOKEN_EXPIRY_MINUTES = 5;

    private static final String LOGIN_ATTEMPTS_PREFIX = "login:attempts:";
    private static final String LOGIN_BLOCKED_PREFIX = "login:blocked:";
    private static final int LOGIN_MAX_ATTEMPTS = 3;
    private static final int LOGIN_BLOCK_MINUTES = 10;

    @Value("${app.otp.expiry-minutes:15}")
    private int expiryMinutes;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (request.getRole() == UserRole.ADMIN) {
            throw new BadRequestException("Cannot register as ADMIN. Contact an existing admin to create an admin account.", "error.auth.register.admin");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered", "error.auth.duplicate.email");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken", "error.auth.duplicate.username");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = User.builder()
                .username(request.getUsername())
                .name(request.getName())
                .email(request.getEmail())
                .password(hashedPassword)
                .role(request.getRole())
                .isVerified(false)
                .isActive(false)
                .build();

        userRepository.save(user);

        String otpCode = otpService.generateOtp(user.getEmail());

        userEventProducer.sendUserRegistered(
                new UserRegisteredEvent(user.getEmail(), user.getUsername(), user.getName(), otpCode, expiryMinutes));

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String identifier = request.getUsernameOrEmail();

        String blockedKey = LOGIN_BLOCKED_PREFIX + identifier;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blockedKey))) {
            throw new BadRequestException("Too many login attempts. Please try again in 10 minutes.", "error.auth.login.blocked");
        }

        User user;
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UnauthorizedException("User not registered", "error.auth.login.not.registered"));
        } else {
            user = userRepository.findByUsername(identifier)
                    .orElseThrow(() -> new UnauthorizedException("User not registered", "error.auth.login.not.registered"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            recordLoginAttempt(identifier);
            throw new UnauthorizedException("Password incorrect", "error.auth.login.wrong.password");
        }

        resetLoginAttempts(identifier);

        if (!user.isVerified()) {
            throw new ForbiddenException("Email not verified. Please verify your email first.", "error.auth.email.not.verified");
        }

        if (!user.isActive()) {
            throw new ForbiddenException("Account is deactivated. Contact support.", "error.auth.account.deactivated");
        }

        String accessToken = jwtService.generateAccessToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .accessToken(accessToken)
                .tokenType("Bearer")
                .build();
    }

    private void recordLoginAttempt(String identifier) {
        String attemptsKey = LOGIN_ATTEMPTS_PREFIX + identifier;
        Long count = redisTemplate.opsForValue().increment(attemptsKey);
        if (count != null && count == 1) {
            redisTemplate.expire(attemptsKey, LOGIN_BLOCK_MINUTES, TimeUnit.MINUTES);
        }
        if (count != null && count >= LOGIN_MAX_ATTEMPTS) {
            String blockedKey = LOGIN_BLOCKED_PREFIX + identifier;
            redisTemplate.opsForValue().set(blockedKey, "1", LOGIN_BLOCK_MINUTES, TimeUnit.MINUTES);
        }
    }

    private void resetLoginAttempts(String identifier) {
        redisTemplate.delete(LOGIN_ATTEMPTS_PREFIX + identifier);
        redisTemplate.delete(LOGIN_BLOCKED_PREFIX + identifier);
    }

    @Override
    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail(), "error.auth.user.not.found", request.getEmail()));

        if (user.isVerified()) {
            throw new BadRequestException("Email already verified", "error.auth.email.verified");
        }

        if (!otpService.isVerifyAllowed(request.getEmail())) {
            throw new BadRequestException("Too many verification attempts. Please try again in 5 minutes.", "error.auth.verify.blocked");
        }

        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp());
        if (!valid) {
            otpService.recordVerifyAttempt(request.getEmail());
            throw new BadRequestException("Invalid or expired OTP", "error.auth.otp.invalid");
        }

        otpService.resetVerifyAttempts(request.getEmail());
        user.setVerified(true);
        user.setActive(true);
        userRepository.save(user);

        userEventProducer.sendUserVerified(
                new UserVerifiedEvent(user.getEmail(), user.getUsername(), user.getName()));

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (!otpService.isResendAllowed(request.getEmail())) {
            throw new BadRequestException("Resend OTP limit reached. Please try again in 1 hour.", "error.auth.resend.blocked");
        }

        otpService.recordResendAttempt(request.getEmail());
        String otpCode = otpService.generateOtp(user.getEmail());

        userEventProducer.sendUserResendOtp(
                new UserResendOtpEvent(user.getEmail(), user.getUsername(), user.getName(), otpCode, expiryMinutes));

        return AuthResponse.builder().build();
    }

    @Override
    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        String otpCode = otpService.generateOtp(user.getEmail());

        userEventProducer.sendUserForgotPassword(
                new UserForgotPasswordEvent(user.getEmail(), user.getUsername(), user.getName(), otpCode, expiryMinutes));

        return AuthResponse.builder().build();
    }

    @Override
    public VerifyOtpResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail(), "error.auth.user.not.found", request.getEmail()));

        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new BadRequestException("Invalid or expired OTP", "error.auth.otp.invalid");
        }

        String token = UUID.randomUUID().toString();
        String key = RESET_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, user.getEmail(), RESET_TOKEN_EXPIRY_MINUTES, TimeUnit.MINUTES);

        return VerifyOtpResponse.builder()
                .token(token)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String key = RESET_TOKEN_PREFIX + request.getToken();
        String email = redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new BadRequestException("Invalid or expired token", "error.auth.token.invalid");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email, "error.auth.user.not.found", email));

        String hashedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPassword(hashedPassword);
        userRepository.save(user);

        redisTemplate.delete(key);

        return AuthResponse.builder().build();
    }
}
