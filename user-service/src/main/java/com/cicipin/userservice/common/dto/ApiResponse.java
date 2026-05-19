package com.cicipin.userservice.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private int status;
    private T data;
    private Object errors;

    public static <T> ApiResponse<T> success(HttpStatus status, T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .status(status.value())
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(HttpStatus status, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(status.value())
                .build();
    }

    public static <T> ApiResponse<T> validationError(Map<String, String> fieldErrors, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .status(HttpStatus.UNPROCESSABLE_CONTENT.value())
                .errors(fieldErrors)
                .build();
    }
}
