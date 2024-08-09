package com.daou.sabangnetserver.global.error;

import com.daou.sabangnetserver.global.common.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationExceptions(MethodArgumentNotValidException exception) {

        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getBindingResult().getAllErrors().getFirst().getDefaultMessage())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleRuntimeExceptions(RuntimeException exception) {
        return ErrorResponse.builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleUserNotFoundException(UserNotFoundException exception) {
        return ErrorResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(DuplicationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleDuplicationException(DuplicationException exception) {
        return ErrorResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(AuthorityNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleAuthorityNotFoundException(AuthorityNotFoundException exception) {
        return ErrorResponse.builder()
                .code(exception.getCode())
                .message(exception.getMessage())
                .build();
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGenericException(Exception exception) {
        return ErrorResponse.builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value()) // 500
                .message("서버 내부 오류입니다.")
                .build();
    }

}
