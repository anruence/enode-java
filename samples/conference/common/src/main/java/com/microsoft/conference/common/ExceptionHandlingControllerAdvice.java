package com.microsoft.conference.common;

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
    public ActionResult errorHandlerForUncaughtException(Exception exception) {
        log.error("Unhandled exception occurs.", exception);
        return ActionResult.error("500", exception.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(value = BindException.class)
    public ActionResult errorBindException(BindException exception) {
        log.error("BindException", exception);
        String errors = exception.getFieldErrors().stream().map(e -> e.getField() + e.getDefaultMessage()).collect(Collectors.joining(","));
        return ActionResult.error("500", errors);
    }

    @ResponseBody
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    public ActionResult errorMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        log.error("MissingServletRequestParameterException", exception);
        return ActionResult.error("500", exception.getMessage());
    }

}
