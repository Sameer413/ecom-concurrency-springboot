package org.example.flashsale.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.example.flashsale.common.persistence.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        return new ResponseEntity<Map<String, String>>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                "RESOURCE_NOT_FOUND",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateResource(
            DuplicateResourceException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                "DUPLICATE_RESOURCE",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        ApiResponse.error(
                                "Duplicate resource",
                                response
                        )
                );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateResource(
            UnauthorizedException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.UNAUTHORIZED.value(),
                "UNAUTHORIZED",
                ex.getMessage(),
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                "Unauthorized",
                                response
                        )
                );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleDuplicateResource(
            AccessDeniedException ex,
            HttpServletRequest request) {

        ErrorResponse response = new ErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                "ACCESS_DENIED",
                "You do not have permission to access this resource",
                request.getRequestURI(),
                Map.of()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(
                        ApiResponse.error(
                                "Access Denied",
                                response
                        )
                );
    }
}
