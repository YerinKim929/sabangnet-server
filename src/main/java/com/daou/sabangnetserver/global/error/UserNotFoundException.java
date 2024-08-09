package com.daou.sabangnetserver.global.error;

import lombok.Getter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@Getter
public class UserNotFoundException extends UsernameNotFoundException {
    private final int code;

    public UserNotFoundException(int code, String message) {
        super(message);
        this.code = code;
    }
}
