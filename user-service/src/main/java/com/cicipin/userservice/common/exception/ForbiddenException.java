package com.cicipin.userservice.common.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {
    private final String messageCode;
    private final Object[] args;

    public ForbiddenException(String message) {
        super(message);
        this.messageCode = message;
        this.args = null;
    }

    public ForbiddenException(String message, String messageCode, Object... args) {
        super(message);
        this.messageCode = messageCode;
        this.args = args;
    }
}
