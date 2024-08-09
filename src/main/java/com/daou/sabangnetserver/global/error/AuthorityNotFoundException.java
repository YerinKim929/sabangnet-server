package com.daou.sabangnetserver.global.error;

import lombok.Getter;

@Getter
public class AuthorityNotFoundException extends RuntimeException {
    private final int code;

    public AuthorityNotFoundException(int code, String message) {
        super(message);
        this.code = code;
    }
}
