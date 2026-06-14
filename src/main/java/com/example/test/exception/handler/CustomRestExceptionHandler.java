package com.example.test.exception.handler;

import com.example.test.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class CustomRestExceptionHandler {

    private static final String LOG_TEXT = "CONTROLLER EXCEPTION HANDLER";

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> exceptionHandler(Exception ex) {
        log.error(LOG_TEXT, ex);
        ErrorResponse error = new ErrorResponse();
        error.setExceptionType(ex.getClass().getSimpleName());
        error.setMessage(error.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
