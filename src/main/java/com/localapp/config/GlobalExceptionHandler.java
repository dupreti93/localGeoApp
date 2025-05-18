package com.localapp.config;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e, ServletWebRequest request) {
        String userId = getUserId();
        logger.error("Unexpected error for user {} on request {}: {}", userId, request.getRequest().getRequestURI(), e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e, ServletWebRequest request) {
        String userId = getUserId();
        logger.warn("Invalid argument for user {} on request {}: {}", userId, request.getRequest().getRequestURI(), e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid input: " + e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> handleValidationException(ConstraintViolationException e, ServletWebRequest request) {
        String userId = getUserId();
        String message = e.getConstraintViolations().stream()
                .map(v -> String.format("%s: %s", v.getPropertyPath(), v.getMessage()))
                .collect(Collectors.joining("; "));
        logger.warn("Validation error for user {} on request {}: {}", userId, request.getRequest().getRequestURI(), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleMethodArgumentNotValid(MethodArgumentNotValidException e, ServletWebRequest request) {
        String userId = getUserId();
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String field = error instanceof FieldError ? ((FieldError) error).getField() : "object";
                    String msg = error.getDefaultMessage();
                    return String.format("%s: %s", field, msg != null ? msg : "Invalid value");
                })
                .collect(Collectors.joining("; "));
        logger.warn("Validation error for user {} on request {}: {}", userId, request.getRequest().getRequestURI(), message, e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Validation error: " + message);
    }

    private String getUserId() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getName();
        } catch (Exception e) {
            return "anonymous";
        }
    }
}