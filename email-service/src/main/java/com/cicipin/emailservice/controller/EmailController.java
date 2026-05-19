package com.cicipin.emailservice.controller;

import com.cicipin.emailservice.dto.ApiResponse;
import com.cicipin.emailservice.dto.SendEmailRequest;
import com.cicipin.emailservice.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
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
    private final MessageSource messageSource;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> send(@Valid @RequestBody SendEmailRequest request) {
        log.info("Received email send request: type={}, to={}", request.getType(), request.getTo());
        emailService.send(request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, null, resolve("success.email.sent"))
        );
    }

    private String resolve(String code) {
        try {
            return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return code;
        }
    }
}
