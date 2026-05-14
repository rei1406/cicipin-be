package com.cicipin.emailservice.controller;

import com.cicipin.emailservice.dto.SendEmailRequest;
import com.cicipin.emailservice.dto.SendEmailResponse;
import com.cicipin.emailservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal REST endpoint for sending emails.
 * This service is NOT exposed through the API Gateway — it is only
 * reachable by other services inside the Docker network.
 *
 * POST /api/email/send
 *   Body: one of OtpEmailRequest | WelcomeEmailRequest |
 *               PasswordResetEmailRequest | GenericEmailRequest
 *   (discriminated by the "type" field)
 */
@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<SendEmailResponse> send(@Valid @RequestBody SendEmailRequest request) {
        log.info("Received email send request: type={}, to={}", request.getType(), request.getTo());
        emailService.send(request);
        return ResponseEntity.ok(
            SendEmailResponse.builder()
                .success(true)
                .message("Email sent successfully")
                .build()
        );
    }
}
