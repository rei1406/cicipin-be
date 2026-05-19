package com.cicipin.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("JwtProvider Unit Tests")
class JwtProviderTest {

    private static final String SECRET = "dGhpcyBpcyBhIGRldmVsb3BtZW50IGp3dCBzZWNyZXQga2V5IGZvciBjaWNpcGlu";

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET);
    }

    @Test
    @DisplayName("should validate token and return claims when token is valid")
    void shouldValidateToken_whenTokenIsValid() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String token = Jwts.builder()
                .subject("550e8400-e29b-41d4-a716-446655440000")
                .claim("email", "test@example.com")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        var claims = jwtProvider.validateToken(token);

        assertThat(claims).isNotNull();
        assertThat(claims.getSubject()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("should throw JwtValidationException when token signature is invalid")
    void shouldThrowException_whenSignatureIsInvalid() {
        var differentKey = Keys.hmacShaKeyFor("other-secret-key-that-is-at-least-thirty-two-bytes-long!!".getBytes());
        String token = Jwts.builder()
                .subject("user-id")
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(differentKey)
                .compact();

        assertThatThrownBy(() -> jwtProvider.validateToken(token))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    @DisplayName("should throw JwtValidationException when token is expired")
    void shouldThrowException_whenTokenIsExpired() {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String token = Jwts.builder()
                .subject("user-id")
                .claim("role", "ADMIN")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtProvider.validateToken(token))
                .isInstanceOf(JwtValidationException.class);
    }

    @Test
    @DisplayName("should throw JwtValidationException when token is malformed")
    void shouldThrowException_whenTokenIsMalformed() {
        assertThatThrownBy(() -> jwtProvider.validateToken("not-a-valid-jwt-token"))
                .isInstanceOf(JwtValidationException.class);
    }
}
