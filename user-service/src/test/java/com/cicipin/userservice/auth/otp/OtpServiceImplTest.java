package com.cicipin.userservice.auth.otp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("OtpServiceImpl Unit Tests")
class OtpServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private OtpServiceImpl otpService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(otpService, "expiryMinutes", 15);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Nested
    @DisplayName("generateOtp()")
    class GenerateOtp {

        @Test
        @DisplayName("should store OTP in Redis with correct key prefix and email")
        void shouldStoreOtp_withCorrectKey() {
            String email = "john@example.com";
            String expectedKey = "otp:john@example.com";

            otpService.generateOtp(email);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(valueOperations).set(keyCaptor.capture(), anyString(), eq(15L), eq(TimeUnit.MINUTES));
            assertThat(keyCaptor.getValue()).isEqualTo(expectedKey);
        }

        @Test
        @DisplayName("should store OTP with 15 minutes expiry")
        void shouldStoreOtp_with15MinutesExpiry() {
            otpService.generateOtp("test@example.com");

            verify(valueOperations).set(anyString(), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        }

        @Test
        @DisplayName("should return a 6-digit OTP")
        void shouldReturn6DigitOtp() {
            String otp = otpService.generateOtp("test@example.com");

            assertThat(otp).matches("\\d{6}");
        }

        @Test
        @DisplayName("should return different OTP on each call")
        void shouldReturnDifferentOtp_onEachCall() {
            String otp1 = otpService.generateOtp("a@b.com");
            String otp2 = otpService.generateOtp("a@b.com");

            assertThat(otp1).isNotEqualTo(otp2);
        }

        @Test
        @DisplayName("should use the email provider Redis key format")
        void shouldUseEmailInRedisKey() {
            String email = "user+alias@domain.co.id";
            otpService.generateOtp(email);

            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            verify(valueOperations).set(keyCaptor.capture(), anyString(), eq(15L), eq(TimeUnit.MINUTES));
            assertThat(keyCaptor.getValue()).isEqualTo("otp:" + email);
        }

        @Test
        @DisplayName("should overwrite existing OTP for the same email")
        void shouldOverwriteExistingOtp_forSameEmail() {
            otpService.generateOtp("same@email.com");
            otpService.generateOtp("same@email.com");

            verify(valueOperations, times(2)).set(eq("otp:same@email.com"), anyString(), eq(15L), eq(TimeUnit.MINUTES));
        }
    }
}
