package com.cowhealth.common;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, ConstraintViolationException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleValidation(Exception ex) {
        return ApiResponse.fail(400, "Invalid request: " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAny(Exception ex) {
        log.error("Unhandled error", ex);
        return ApiResponse.fail(500, "Internal server error");
    }
}
