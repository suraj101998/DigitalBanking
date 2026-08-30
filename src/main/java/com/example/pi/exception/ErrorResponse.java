package com.example.pi.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/**
 * Standardised error response body (doc item #27).
 *
 * {
 *   "timestamp": "2026-08-30T17:30:22Z",
 *   "status": 400,
 *   "code": "INSUFFICIENT_BALANCE",
 *   "message": "Insufficient balance",
 *   "path": "/api/v1/payments",
 *   "traceId": "abc123"
 * }
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String timestamp;
    private int status;
    private String code;
    private String message;
    private String path;
    /** Populated from MDC correlationId set by CorrelationIdFilter (doc item #21). */
    private String traceId;
    private Map<String, String> fieldErrors;

    public ErrorResponse() {
    }

    public ErrorResponse(String timestamp, int status, String code,
                         String message, String path, String traceId) {
        this.timestamp = timestamp;
        this.status = status;
        this.code = code;
        this.message = message;
        this.path = path;
        this.traceId = traceId;
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public Map<String, String> getFieldErrors() { return fieldErrors; }
    public void setFieldErrors(Map<String, String> fieldErrors) { this.fieldErrors = fieldErrors; }
}
