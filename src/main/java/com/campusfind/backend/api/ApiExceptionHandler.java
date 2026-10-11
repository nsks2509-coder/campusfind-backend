package com.campusfind.backend.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(this::messageFor)
                .orElse("Please check the submitted fields."));
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleStatus(ResponseStatusException exception) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", exception.getReason() == null ? "Request failed." : exception.getReason());
        return ResponseEntity.status(exception.getStatusCode()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception exception) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("message", "The server could not process the request.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private String messageFor(FieldError error) {
        String field = error.getField();
        return error.getDefaultMessage() == null ? "Invalid field: " + field : field + ": " + error.getDefaultMessage();
    }
}