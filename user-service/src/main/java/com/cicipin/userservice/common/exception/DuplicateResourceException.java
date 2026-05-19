package com.cicipin.userservice.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String messageCode;
    private final Object[] args;

    public DuplicateResourceException(String message) {
        super(message);
        this.messageCode = message;
        this.args = null;
    }

    public DuplicateResourceException(String message, String messageCode, Object... args) {
        super(message);
        this.messageCode = messageCode;
        this.args = args;
    }
}
