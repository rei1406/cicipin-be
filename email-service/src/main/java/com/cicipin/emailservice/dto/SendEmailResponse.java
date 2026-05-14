package com.cicipin.emailservice.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Response returned after an email send attempt.
 */
@Data
@Builder
public class SendEmailResponse {

    private boolean success;
    private String message;
}
