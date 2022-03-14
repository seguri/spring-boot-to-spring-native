package com.milleniumcare.claims.logging;

import org.springframework.boot.logging.LogLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface LogEntryExit {
    LogLevel value() default LogLevel.INFO;
}

