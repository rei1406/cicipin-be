package com.cicipin.userservice.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedException extends RuntimeException {
    private final String messageCode;
    private final Object[] args;

    public UnauthorizedException(String message) {
        super(message);
        this.messageCode = message;
        this.args = null;
    }

    public UnauthorizedException(String message, String messageCode, Object... args) {
        super(message);
        this.messageCode = messageCode;
        this.args = args;
    }
}
