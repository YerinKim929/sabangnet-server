package com.daou.sabangnetserver.global.error;

import lombok.Getter;

@Getter
public class DuplicationException extends RuntimeException {
    private final int code;

    public DuplicationException(int code, String message) {
        super(message);
        this.code = code;
    }
}
