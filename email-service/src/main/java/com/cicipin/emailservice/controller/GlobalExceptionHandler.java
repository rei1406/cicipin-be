package com.cicipin.emailservice.controller;

import com.cicipin.emailservice.dto.SendEmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SendEmailResponse> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining("; "));
        log.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(
            SendEmailResponse.builder()
                .success(false)
                .message("Validation failed: " + errors)
                .build()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<SendEmailResponse> handleRuntime(RuntimeException ex) {
        log.error("Email send failed: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            SendEmailResponse.builder()
                .success(false)
                .message("Failed to send email: " + ex.getMessage())
                .build()
        );
    }
}
