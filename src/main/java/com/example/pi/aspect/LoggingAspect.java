package com.example.pi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Logging Aspect for method-level timing and exception logging.
 *
 * Improvements (doc items #45, #46, #47):
 *  - Removed duplicate pointcut: the original had two overlapping @Around advices that
 *    caused service methods to be logged twice. Now a single pointcut covers all layers.
 *  - Sensitive arguments (passwords, keys, tokens) are NOT logged — args are excluded
 *    from DEBUG output to avoid PII/credential exposure.
 *  - Method result types are logged but result values are NOT to avoid logging PII.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /** Argument names that should never appear in logs. */
    private static final Set<String> SENSITIVE_METHOD_NAMES = Set.of(
            "login", "register", "publicRegister", "changePassword", "resetPassword"
    );

    /**
     * Single consolidated pointcut covering service, controller, and repository layers.
     * The original had two overlapping @Around advices causing duplicate service-layer logs.
     * Filters and security classes are excluded to avoid AOP proxy issues.
     */
    @Around("(execution(* com.example.pi.service..*(..)) || " +
            "execution(* com.example.pi.controller..*(..)) || " +
            "execution(* com.example.pi.repository..*(..))) && " +
            "!execution(* com.example.pi.filter..*(..)) && " +
            "!execution(* com.example.pi.security..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String signature  = className + "." + methodName;

        long startTime = System.currentTimeMillis();

        // Only log argument count — never values — to avoid PII/credential exposure (doc item #45)
        int argCount = joinPoint.getArgs() != null ? joinPoint.getArgs().length : 0;
        boolean isSensitive = SENSITIVE_METHOD_NAMES.contains(methodName);

        if (!isSensitive) {
            logger.debug(">>> ENTERING: {} (args={})", signature, argCount);
        } else {
            logger.debug(">>> ENTERING: {} [args redacted]", signature);
        }

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - startTime;
            // Log result type only — not the result value (could contain PII)
            String resultType = result != null ? result.getClass().getSimpleName() : "void";
            logger.debug("<<< EXITING:  {} | {}ms | result-type={}", signature, elapsed, resultType);
            return result;

        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startTime;
            logger.error("!!! EXCEPTION: {} | {}ms | {}: {}", signature, elapsed,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
