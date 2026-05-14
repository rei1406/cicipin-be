package com.cicipin.emailservice.service;

import com.cicipin.emailservice.dto.SendEmailRequest;

/**
 * Contract for sending emails.
 */
public interface EmailService {

    /**
     * Sends an email based on the given request.
     * The concrete type of {@code request} determines which template is used.
     *
     * @param request the email request (OTP, welcome, password-reset, or generic)
     */
    void send(SendEmailRequest request);
}
