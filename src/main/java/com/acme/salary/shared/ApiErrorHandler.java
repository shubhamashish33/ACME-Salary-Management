package com.acme.salary.shared;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
    public record ErrorBody(String code, String message, Map<String,String> fieldErrors, String correlationId, Instant timestamp) {}
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorBody> invalid(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var fields = new java.util.LinkedHashMap<String,String>();
        ex.getBindingResult().getFieldErrors().forEach(e -> fields.putIfAbsent(e.getField(), e.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Review the highlighted fields.", fields, request);
    }
    @ExceptionHandler({IllegalArgumentException.class, ConstraintViolationException.class})
    ResponseEntity<ErrorBody> badRequest(Exception ex, HttpServletRequest request) { return error(HttpStatus.BAD_REQUEST,"INVALID_REQUEST",ex.getMessage(),Map.of(),request); }
    @ExceptionHandler(java.util.NoSuchElementException.class)
    ResponseEntity<ErrorBody> notFound(Exception ex, HttpServletRequest request) { return error(HttpStatus.NOT_FOUND,"NOT_FOUND",ex.getMessage(),Map.of(),request); }
    private ResponseEntity<ErrorBody> error(HttpStatus status, String code, String message, Map<String,String> fields, HttpServletRequest request) {
        var id = request.getHeader("X-Correlation-Id"); if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        return ResponseEntity.status(status).header("X-Correlation-Id", id).body(new ErrorBody(code,message,fields,id,Instant.now()));
    }
}

