package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.AuthResponse;
import com.cicipin.userservice.auth.dto.RegisterRequest;
import com.cicipin.userservice.auth.otp.OtpService;
import com.cicipin.userservice.common.exception.DuplicateResourceException;
import com.cicipin.userservice.common.model.User;
import com.cicipin.userservice.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken");
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

        otpService.generateOtp(user.getEmail());

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .message("Registration successful. Please check your email for verification.")
                .build();
    }
}
