package com.example.pi.exception;

import com.example.pi.filter.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralised error handler (doc items #26, #27, #28).
 * All exceptions propagate here — controllers must NOT catch exceptions.
 * Uses @RestControllerAdvice so the response body is automatically serialised to JSON.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Domain exceptions ──────────────────────────────────────────────────────

    @ExceptionHandler(CustomerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCustomerNotFound(
            CustomerNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotFound(
            AccountNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_BALANCE", ex.getMessage(), request);
    }

    @ExceptionHandler(DuplicateTransactionException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateTransaction(
            DuplicateTransactionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_TRANSACTION", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransaction(
            InvalidTransactionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_TRANSACTION", ex.getMessage(), request);
    }

    @ExceptionHandler(UnauthorizedAccountAccessException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedAccess(
            UnauthorizedAccountAccessException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACCESS", ex.getMessage(), request);
    }

    @ExceptionHandler(ConcurrentTransactionException.class)
    public ResponseEntity<ErrorResponse> handleConcurrentTransaction(
            ConcurrentTransactionException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "CONCURRENT_TRANSACTION", ex.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
    }

    // ── Validation errors ──────────────────────────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError ? ((FieldError) error).getField() : error.getObjectName();
            fieldErrors.put(field, error.getDefaultMessage());
        });
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                "Request validation failed",
                request.getRequestURI(),
                null
        );
        body.setFieldErrors(fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    /**
     * Handles every uncaught exception so the server never returns an empty 500 body.
     * Bug fix: the original handler was missing @ExceptionHandler(Exception.class) (doc item #26).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest request) {
        logger.error("Unhandled exception on {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "An unexpected error occurred", request);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String code, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                Instant.now().toString(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                MDC.get(CorrelationIdFilter.MDC_KEY)   // correlation ID from request thread (doc item #21)
        );
        return ResponseEntity.status(status).body(body);
    }
}
