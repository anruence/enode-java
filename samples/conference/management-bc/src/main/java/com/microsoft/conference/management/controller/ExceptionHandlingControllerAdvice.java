package com.microsoft.conference.management.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class ExceptionHandlingControllerAdvice {

    @ResponseBody
    @ExceptionHandler(value = Exception.class)
    public Result errorHandlerForUncaughtException(Exception exception) {

        log.error("Unhandled exception occurs.", exception);
        return Result.error("500", exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public Result errorBindException(BindException exception) {
        log.error("BindException", exception);
        String errors = exception.getFieldErrors().stream().map(e -> e.getField() + e.getDefaultMessage()).collect(
                Collectors.joining(","));
        return Result.error("500", errors);
    }

    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public Result errorMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        log.error("MissingServletRequestParameterException", exception);
        return Result.error("500", exception.getMessage());
    }

}
