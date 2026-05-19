package com.cicipin.userservice.auth;

import com.cicipin.userservice.auth.dto.AuthResponse;
import com.cicipin.userservice.auth.dto.LoginRequest;
import com.cicipin.userservice.auth.dto.RegisterRequest;
import com.cicipin.userservice.auth.otp.OtpService;
import com.cicipin.userservice.common.exception.BadRequestException;
import com.cicipin.userservice.common.exception.DuplicateResourceException;
import com.cicipin.userservice.common.exception.ForbiddenException;
import com.cicipin.userservice.common.exception.UnauthorizedException;
import com.cicipin.userservice.common.model.User;
import com.cicipin.userservice.common.model.UserRole;
import com.cicipin.userservice.kafka.UserEventProducer;
import com.cicipin.userservice.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import org.mockito.InOrder;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OtpService otpService;

    @Mock
    private UserEventProducer userEventProducer;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .username("johndoe")
                .name("John Doe")
                .email("john@example.com")
                .password("password123")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @BeforeEach
        void setUp() {
            lenient().when(redisTemplate.hasKey(anyString())).thenReturn(false);
            lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
            lenient().when(redisTemplate.delete(anyString())).thenReturn(true);
        }

        @Test
        @DisplayName("should return AuthResponse with JWT token when credentials are valid (by email)")
        void shouldReturnAuthResponseWithToken_whenCredentialsAreValid() {
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .username("johndoe")
                    .name("John Doe")
                    .email("john@example.com")
                    .password("$2a$10$hashed")
                    .role(UserRole.CUSTOMER)
                    .isVerified(true)
                    .isActive(true)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("test-jwt-token");

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("john@example.com")
                    .password("password123")
                    .build();

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
            verify(jwtService, times(1)).generateAccessToken(user);
            verify(redisTemplate, times(2)).delete(anyString());
        }

        @Test
        @DisplayName("should return AuthResponse with JWT token when credentials are valid (by username)")
        void shouldReturnAuthResponseWithToken_whenLoginByUsername() {
            UUID userId = UUID.randomUUID();
            User user = User.builder()
                    .id(userId)
                    .username("johndoe")
                    .name("John Doe")
                    .email("john@example.com")
                    .password("$2a$10$hashed")
                    .role(UserRole.CUSTOMER)
                    .isVerified(true)
                    .isActive(true)
                    .build();

            when(userRepository.findByUsername("johndoe")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);
            when(jwtService.generateAccessToken(user)).thenReturn("test-jwt-token");

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("johndoe")
                    .password("password123")
                    .build();

            AuthResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getAccessToken()).isEqualTo("test-jwt-token");
            assertThat(response.getTokenType()).isEqualTo("Bearer");
        }

        @Test
        @DisplayName("should throw UnauthorizedException when email is not found")
        void shouldThrowException_whenEmailNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("unknown@example.com")
                    .password("password123")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not registered");

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("should throw UnauthorizedException when username is not found")
        void shouldThrowException_whenUsernameNotFound() {
            when(userRepository.findByUsername("unknownuser")).thenReturn(Optional.empty());

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("unknownuser")
                    .password("password123")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("User not registered");

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("should throw UnauthorizedException when password is wrong")
        void shouldThrowException_whenPasswordIsWrong() {
            User user = User.builder()
                    .email("john@example.com")
                    .password("$2a$10$hashed")
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", user.getPassword())).thenReturn(false);
            when(valueOperations.increment(anyString())).thenReturn(1L);

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("john@example.com")
                    .password("wrongpassword")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Password incorrect");

            verifyNoInteractions(jwtService);
            verify(valueOperations).increment("login:attempts:john@example.com");
        }

        @Test
        @DisplayName("should throw BadRequestException when login is blocked after too many attempts")
        void shouldThrowException_whenLoginIsBlocked() {
            when(redisTemplate.hasKey("login:blocked:john@example.com")).thenReturn(true);

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("john@example.com")
                    .password("password123")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Too many login attempts. Please try again in 10 minutes.");

            verifyNoInteractions(jwtService);
            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("should throw ForbiddenException when email is not verified")
        void shouldThrowException_whenEmailNotVerified() {
            User user = User.builder()
                    .email("john@example.com")
                    .password("$2a$10$hashed")
                    .isVerified(false)
                    .isActive(true)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("john@example.com")
                    .password("password123")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Email not verified. Please verify your email first.");

            verifyNoInteractions(jwtService);
        }

        @Test
        @DisplayName("should throw ForbiddenException when account is inactive")
        void shouldThrowException_whenAccountIsInactive() {
            User user = User.builder()
                    .email("john@example.com")
                    .password("$2a$10$hashed")
                    .isVerified(true)
                    .isActive(false)
                    .build();

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", user.getPassword())).thenReturn(true);

            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("john@example.com")
                    .password("password123")
                    .build();

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(ForbiddenException.class)
                    .hasMessage("Account is deactivated. Contact support.");

            verifyNoInteractions(jwtService);
        }
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("should throw BadRequestException when registering as ADMIN")
        void shouldThrowException_whenRegisteringAsAdmin() {
            RegisterRequest adminRequest = RegisterRequest.builder()
                    .username("adminuser")
                    .name("Admin User")
                    .email("admin@example.com")
                    .password("password123")
                    .role(UserRole.ADMIN)
                    .build();

            assertThatThrownBy(() -> authService.register(adminRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage("Cannot register as ADMIN. Contact an existing admin to create an admin account.");

            verifyNoInteractions(userRepository);
            verifyNoInteractions(passwordEncoder);
            verifyNoInteractions(otpService);
        }

        @Test
        @DisplayName("should create user, generate OTP, and return AuthResponse when request is valid")
        void shouldCreateUserAndGenerateOtp_whenRequestIsValid() {
            String hashedPassword = "$2a$10$hashedPassword";
            UUID userId = UUID.randomUUID();
            String generatedOtp = "123456";

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(hashedPassword);
            when(otpService.generateOtp(validRequest.getEmail())).thenReturn(generatedOtp);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(userId);
                return user;
            });

            AuthResponse response = authService.register(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getName()).isEqualTo("John Doe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER);

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getUsername()).isEqualTo("johndoe");
            assertThat(savedUser.getName()).isEqualTo("John Doe");
            assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
            assertThat(savedUser.getPassword()).isEqualTo(hashedPassword);
            assertThat(savedUser.getRole()).isEqualTo(UserRole.CUSTOMER);
            assertThat(savedUser.isVerified()).isFalse();
            assertThat(savedUser.isActive()).isFalse();

            verify(otpService, times(1)).generateOtp(validRequest.getEmail());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void shouldThrowException_whenEmailAlreadyExists() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).existsByUsername(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verifyNoInteractions(otpService);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when username already exists")
        void shouldThrowException_whenUsernameAlreadyExists() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

            assertThatThrownBy(() -> authService.register(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Username already taken");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
            verifyNoInteractions(otpService);
        }

        @Test
        @DisplayName("should hash password before saving")
        void shouldHashPassword_beforeSaving() {
            String rawPassword = validRequest.getPassword();
            String hashedPassword = "$2a$10$differentHashedValue";

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
            when(otpService.generateOtp(anyString())).thenReturn("000000");

            authService.register(validRequest);

            verify(passwordEncoder, times(1)).encode(rawPassword);
            verify(userRepository).save(argThat(user ->
                    user.getPassword().equals(hashedPassword) &&
                    !user.getPassword().equals(rawPassword)
            ));
        }

        @Test
        @DisplayName("should set default values for new user")
        void shouldSetDefaultValues_forNewUser() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(otpService.generateOtp(anyString())).thenReturn("000000");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            authService.register(validRequest);
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.isVerified()).isFalse();
            assertThat(savedUser.isActive()).isFalse();
            assertThat(savedUser.getPhoto()).isNull();
        }

        @Test
        @DisplayName("should call userRepository.save exactly once")
        void shouldCallSaveExactlyOnce() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(otpService.generateOtp(anyString())).thenReturn("000000");

            authService.register(validRequest);

            verify(userRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("should check email existence before username existence")
        void shouldCheckEmailBeforeUsername() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
            when(otpService.generateOtp(anyString())).thenReturn("000000");

            authService.register(validRequest);

            verify(userRepository).existsByEmail(validRequest.getEmail());
            verify(userRepository).existsByUsername(validRequest.getUsername());
        }

        @Test
        @DisplayName("should generate OTP with the registered email after saving user")
        void shouldGenerateOtp_withRegisteredEmail_afterSavingUser() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn("hashed");
            when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

            authService.register(validRequest);

            verify(otpService, times(1)).generateOtp(validRequest.getEmail());
            InOrder inOrder = inOrder(userRepository, otpService);
            inOrder.verify(userRepository).save(any());
            inOrder.verify(otpService).generateOtp(validRequest.getEmail());
        }

        @Test
        @DisplayName("should work with all non-ADMIN UserRole values")
        void shouldWorkWithAllRoles() {
            for (UserRole role : UserRole.values()) {
                if (role == UserRole.ADMIN) continue;

                RegisterRequest request = RegisterRequest.builder()
                        .username("user_" + role.name().toLowerCase())
                        .name("User " + role)
                        .email("user_" + role.name().toLowerCase() + "@example.com")
                        .password("password123")
                        .role(role)
                        .build();

                when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
                when(userRepository.existsByUsername(request.getUsername())).thenReturn(false);
                when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed_" + role);

                var savedUser = User.builder()
                        .id(UUID.randomUUID())
                        .username(request.getUsername())
                        .name(request.getName())
                        .email(request.getEmail())
                        .password("hashed_" + role)
                        .role(role)
                        .isVerified(false)
                        .isActive(false)
                        .build();
                when(userRepository.save(any(User.class))).thenReturn(savedUser);
                when(otpService.generateOtp(request.getEmail())).thenReturn("000000");

                AuthResponse response = authService.register(request);
                assertThat(response.getRole()).isEqualTo(role);
            }
        }
    }
}
