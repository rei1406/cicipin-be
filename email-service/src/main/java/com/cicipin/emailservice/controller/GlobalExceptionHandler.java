package com.cicipin.emailservice.controller;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cicipin.emailservice.dto.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        Locale locale = LocaleContextHolder.getLocale();
        ex.getBindingResult().getFieldErrors().forEach(err -> {
            String resolved;
            try {
                resolved = messageSource.getMessage(err, locale);
            } catch (NoSuchMessageException e) {
                resolved = err.getDefaultMessage();
            }
            fieldErrors.put(err.getField(), resolved);
        });
        log.warn("Validation failed: {}", fieldErrors);
        String validationMsg;
        try {
            validationMsg = messageSource.getMessage("error.validation.failed", null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            validationMsg = "Validation Failed";
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(ApiResponse.validationError(fieldErrors, validationMsg));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(RuntimeException ex) {
        log.error("Email send failed: {}", ex.getMessage(), ex);
        String msg;
        try {
            msg = messageSource.getMessage("error.email.send.failed", new Object[]{ex.getMessage()}, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            msg = "Failed to send email: " + ex.getMessage();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, msg));
    }
}
