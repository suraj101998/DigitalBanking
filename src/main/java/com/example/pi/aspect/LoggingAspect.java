package com.example.pi.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Logging Aspect for comprehensive method-level logging.
 * Logs all method entries, exits, execution time, and exceptions.
 * Useful for performance monitoring and debugging service layer operations.
 *
 * NOTE: This aspect deliberately excludes Filters, Security configs, and Framework classes.
 * Only targets business logic in service, controller, and repository packages.
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Log all methods in service, controller, and repository packages.
     * Excludes framework classes and filters to avoid AOP proxy issues.
     */
    @Around("(execution(* com.example.pi.service..*(..)) || " +
            "execution(* com.example.pi.controller..*(..)) || " +
            "execution(* com.example.pi.repository..*(..))) && " +
            "!execution(* com.example.pi.filter..*(..)) && " +
            "!execution(* com.example.pi.security..*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long startTime = System.currentTimeMillis();
        String methodSignature = className + "." + methodName;

        try {
            // Log method entry
            logger.debug(">>> ENTERING METHOD: {} | Arguments: {}", methodSignature, Arrays.toString(args));

            // Execute the method
            Object result = joinPoint.proceed();

            // Log method exit
            long executionTime = System.currentTimeMillis() - startTime;
            logger.debug("<<< EXITING METHOD: {} | Execution Time: {} ms | Result Type: {}",
                    methodSignature, executionTime, result != null ? result.getClass().getSimpleName() : "void");

            return result;

        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("!!! EXCEPTION IN METHOD: {} | Execution Time: {} ms | Exception: {}",
                    methodSignature, executionTime, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * Log all service layer operations with detailed metrics
     */
    @Around("execution(* com.example.pi.service.impl..*(..))")
    public Object logServiceOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startTime = System.currentTimeMillis();
        String methodSignature = className + "." + methodName;

        try {
            logger.info("🔄 SERVICE OPERATION: {} started", methodSignature);
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            logger.info("✅ SERVICE OPERATION: {} completed in {} ms", methodSignature, executionTime);
            return result;

        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            logger.error("❌ SERVICE OPERATION FAILED: {} after {} ms | Error: {}",
                    methodSignature, executionTime, ex.getMessage(), ex);
            throw ex;
        }
    }
}
