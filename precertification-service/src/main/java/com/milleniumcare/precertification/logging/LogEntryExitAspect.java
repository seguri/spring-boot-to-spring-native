package com.milleniumcare.precertification.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.StringJoiner;

@Aspect
@Component
public class LogEntryExitAspect {

    static String entry(String methodName) {
        return (new StringJoiner(" ")
                .add("Started").add(methodName).add("method")).toString();
    }

    static String exit(String methodName) {
        return (new StringJoiner(" ")
                .add("Finished").add(methodName).add("method")).toString();
    }

    static void log(Logger logger, LogLevel level, String message) {
        switch (level) {
            case DEBUG:
                logger.debug(message);
                break;
            case TRACE:
                logger.trace(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
            case FATAL:
                logger.error(message);
                break;
            default:
                logger.info(message);
        }
    }

    @Around("@annotation(com.milleniumcare.precertification.logging.LogEntryExit)")
    public Object log(ProceedingJoinPoint point) throws Throwable {
        var methodSignature = (MethodSignature) point.getSignature();
        Method method = methodSignature.getMethod();
        var annotation = method.getAnnotation(LogEntryExit.class);
        LogLevel level = annotation.value();
        String methodName = method.getName();
        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        log(logger, level, entry(methodName));
        log(logger, level, exit(methodName));

        return point.proceed();
    }
}

