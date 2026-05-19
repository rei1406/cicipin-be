package com.cicipin.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("Gateway Security Integration Tests")
class GatewaySecurityTest {

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("/api/auth/** is public — returns 404/503, not 401")
    void authEndpoints_shouldBePublic() {
        webTestClient.post()
                .uri("/api/auth/login")
                .exchange()
                .expectStatus().value(status ->
                    assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED));
    }

    @Test
    @DisplayName("/api/users/me without token returns 401")
    void usersMe_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("/api/users without token returns 401")
    void users_withoutToken_shouldReturn401() {
        webTestClient.get()
                .uri("/api/users")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @DisplayName("/api/users with non-admin token returns 403")
    void users_withNonAdminToken_shouldReturn403() {
        String token = createToken("550e8400-e29b-41d4-a716-446655440000", "CUSTOMER");

        webTestClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @DisplayName("/api/users/me with valid non-admin token passes authentication (may 5xx without downstream)")
    void usersMe_withValidToken_shouldPassAuth() {
        String token = createToken("550e8400-e29b-41d4-a716-446655440000", "CUSTOMER");

        webTestClient.get()
                .uri("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().value(status ->
                    assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED)
                        .isNotEqualTo(HttpStatus.FORBIDDEN));
    }

    @Test
    @DisplayName("/api/users with ADMIN token passes authentication (may 5xx without downstream)")
    void users_withAdminToken_shouldPassAuth() {
        String token = createToken("550e8400-e29b-41d4-a716-446655440000", "ADMIN");

        webTestClient.get()
                .uri("/api/users")
                .header("Authorization", "Bearer " + token)
                .exchange()
                .expectStatus().value(status ->
                    assertThat(status).isNotEqualTo(HttpStatus.UNAUTHORIZED)
                        .isNotEqualTo(HttpStatus.FORBIDDEN));
    }

    private static String createToken(String subject, String role) {
        var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(
                "dGhpcyBpcyBhIGRldmVsb3BtZW50IGp3dCBzZWNyZXQga2V5IGZvciBjaWNpcGlu"));
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();
    }
}
