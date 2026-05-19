package com.cicipin.userservice.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cicipin.userservice.auth.dto.RegisterRequest;
import com.cicipin.userservice.common.exception.DuplicateResourceException;
import com.cicipin.userservice.common.model.User;
import com.cicipin.userservice.common.model.UserRole;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = RegisterRequest.builder()
                .username("adminuser")
                .name("Admin User")
                .email("admin@example.com")
                .password("password123")
                .role(UserRole.ADMIN)
                .build();
    }

    @Nested
    @DisplayName("createAdmin()")
    class CreateAdmin {

        @Test
        @DisplayName("should create admin user with verified and active defaults")
        void shouldCreateAdminWithVerifiedAndActive() {
            String hashedPassword = "$2a$10$hashed";
            UUID userId = UUID.randomUUID();

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(validRequest.getPassword())).thenReturn(hashedPassword);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(userId);
                return user;
            });

            UserResponse response = userService.createAdmin(validRequest);

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(userId);
            assertThat(response.getUsername()).isEqualTo("adminuser");
            assertThat(response.getEmail()).isEqualTo("admin@example.com");
            assertThat(response.getRole()).isEqualTo(UserRole.ADMIN);

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
            assertThat(savedUser.isVerified()).isTrue();
            assertThat(savedUser.isActive()).isTrue();
            assertThat(savedUser.getPassword()).isEqualTo(hashedPassword);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email already exists")
        void shouldThrowException_whenEmailAlreadyExists() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(true);

            assertThatThrownBy(() -> userService.createAdmin(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).existsByUsername(any());
            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when username already exists")
        void shouldThrowException_whenUsernameAlreadyExists() {
            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(true);

            assertThatThrownBy(() -> userService.createAdmin(validRequest))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Username already taken");

            verify(passwordEncoder, never()).encode(any());
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should hash password before saving")
        void shouldHashPassword_beforeSaving() {
            String rawPassword = validRequest.getPassword();
            String hashedPassword = "$2a$10$differentHashedValue";

            when(userRepository.existsByEmail(validRequest.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(validRequest.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);

            userService.createAdmin(validRequest);

            verify(passwordEncoder, times(1)).encode(rawPassword);
            verify(userRepository).save(argThat(user ->
                    user.getPassword().equals(hashedPassword) &&
                    !user.getPassword().equals(rawPassword)
            ));
        }

        @Test
        @DisplayName("should force ADMIN role regardless of request role")
        void shouldForceAdminRole() {
            RegisterRequest requestWithCustomerRole = RegisterRequest.builder()
                    .username("anotheradmin")
                    .name("Another Admin")
                    .email("another@example.com")
                    .password("password123")
                    .role(UserRole.CUSTOMER)
                    .build();

            when(userRepository.existsByEmail(requestWithCustomerRole.getEmail())).thenReturn(false);
            when(userRepository.existsByUsername(requestWithCustomerRole.getUsername())).thenReturn(false);
            when(passwordEncoder.encode(requestWithCustomerRole.getPassword())).thenReturn("hashed");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userRepository.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            userService.createAdmin(requestWithCustomerRole);

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getRole()).isEqualTo(UserRole.ADMIN);
        }
    }
}
