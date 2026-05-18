package com.cicipin.emailservice.controller;

import com.cicipin.emailservice.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/email-service-health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK,
                        Map.of("status", "UP", "service", "email-service"),
                        "Service is running")
        );
    }
}
