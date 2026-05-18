package com.cicipin.userservice.common;

import com.cicipin.userservice.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/user-service-health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK,
                        Map.of("status", "UP", "service", "user-service"),
                        "Service is running")
        );
    }
}
