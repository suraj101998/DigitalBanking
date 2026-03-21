package com.example.pi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Enumeration;

/**
 * Request/Response Logging Filter for comprehensive HTTP request/response logging.
 * Logs all incoming requests with headers, body, and outgoing responses.
 * Useful for debugging, auditing, and performance monitoring.
 *
 * NOTE: This class is NOT marked with @Component to prevent AOP proxying of Servlet Filters.
 * It is registered via FilterRegistrationBean in SecurityConfiguration.
 */
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // Wrap request and response to cache the body
        HttpServletRequest wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        HttpServletResponse wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request details
            logRequest(wrappedRequest);

            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);

            // Log response details
            logResponse(wrappedResponse, System.currentTimeMillis() - startTime);

        } finally {
            // Copy the content to the original response
            ((ContentCachingResponseWrapper) wrappedResponse).copyBodyToResponse();
        }
    }

    /**
     * Log incoming HTTP request with all details
     */
    private void logRequest(HttpServletRequest request) {
        try {
            StringBuilder requestLog = new StringBuilder();
            requestLog.append("\n");
            requestLog.append("================== INCOMING REQUEST ==================\n");
            requestLog.append(String.format("Timestamp: %s%n", System.currentTimeMillis()));
            requestLog.append(String.format("Request Method: %s%n", request.getMethod()));
            requestLog.append(String.format("Request URL: %s%n", request.getRequestURL()));
            requestLog.append(String.format("Request URI: %s%n", request.getRequestURI()));
            requestLog.append(String.format("Query String: %s%n", request.getQueryString() != null ? request.getQueryString() : "NONE"));
            requestLog.append(String.format("Remote Address: %s%n", request.getRemoteAddr()));
            requestLog.append(String.format("Remote User: %s%n", request.getRemoteUser() != null ? request.getRemoteUser() : "ANONYMOUS"));

            // Log request headers
            requestLog.append("\nRequest Headers:\n");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                // Mask sensitive headers
                if (isSensitiveHeader(headerName)) {
                    requestLog.append(String.format("  %s: ***MASKED***%n", headerName));
                } else {
                    requestLog.append(String.format("  %s: %s%n", headerName, headerValue));
                }
            }

            // Log request body if present
            ContentCachingRequestWrapper cachingRequest = (ContentCachingRequestWrapper) request;
            byte[] content = cachingRequest.getContentAsByteArray();
            if (content != null && content.length > 0) {
                String requestBody = new String(content, "UTF-8");
                requestLog.append(String.format("\nRequest Body (first 1000 chars): %s%n",
                    requestBody.length() > 1000 ? requestBody.substring(0, 1000) + "..." : requestBody));
            }

            requestLog.append("======================================================\n");
            logger.debug(requestLog.toString());

        } catch (Exception e) {
            logger.error("Error logging request", e);
        }
    }

    /**
     * Log outgoing HTTP response with all details
     */
    private void logResponse(HttpServletResponse response, long executionTime) {
        try {
            StringBuilder responseLog = new StringBuilder();
            responseLog.append("\n");
            responseLog.append("================== OUTGOING RESPONSE ==================\n");
            responseLog.append(String.format("Timestamp: %s%n", System.currentTimeMillis()));
            responseLog.append(String.format("HTTP Status Code: %d%n", response.getStatus()));
            responseLog.append(String.format("Execution Time: %d ms%n", executionTime));

            // Log response headers
            responseLog.append("\nResponse Headers:\n");
            response.getHeaderNames().forEach(headerName -> {
                String headerValue = response.getHeader(headerName);
                if (isSensitiveHeader(headerName)) {
                    responseLog.append(String.format("  %s: ***MASKED***%n", headerName));
                } else {
                    responseLog.append(String.format("  %s: %s%n", headerName, headerValue));
                }
            });

            // Log response body if present
            ContentCachingResponseWrapper cachingResponse = (ContentCachingResponseWrapper) response;
            byte[] content = cachingResponse.getContentAsByteArray();
            if (content != null && content.length > 0) {
                String responseBody = new String(content, "UTF-8");
                responseLog.append(String.format("\nResponse Body (first 1000 chars): %s%n",
                    responseBody.length() > 1000 ? responseBody.substring(0, 1000) + "..." : responseBody));
            }

            responseLog.append("======================================================\n");
            logger.debug(responseLog.toString());

        } catch (Exception e) {
            logger.error("Error logging response", e);
        }
    }

    /**
     * Check if header is sensitive (should not be logged)
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseHeaderName = headerName.toLowerCase();
        return lowerCaseHeaderName.contains("password") ||
               lowerCaseHeaderName.contains("authorization") ||
               lowerCaseHeaderName.contains("token") ||
               lowerCaseHeaderName.contains("secret") ||
               lowerCaseHeaderName.contains("api-key") ||
               lowerCaseHeaderName.contains("x-api-key") ||
               lowerCaseHeaderName.contains("cookie");
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("RequestResponseLoggingFilter initialized");
    }

    @Override
    public void destroy() {
        logger.info("RequestResponseLoggingFilter destroyed");
    }
}
