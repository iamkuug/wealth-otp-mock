package com.wealth.demo;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.wealth.demo.ex.BadRequestException;
import com.wealth.demo.ex.GoneRequestException;
import com.wealth.demo.ex.NotFoundException;
import com.wealth.demo.ex.UnauthorizedRequestException;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    protected ResponseEntity<?> handleBadRequestException(BadRequestException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "400", ex.getLocalizedMessage(), ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<?> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND,
                "404", ex.getLocalizedMessage(), ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(UnauthorizedRequestException.class)
    protected ResponseEntity<?> handleUnauthorizedRequestException(UnauthorizedRequestException ex,
            WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.UNAUTHORIZED,
                "401", ex.getLocalizedMessage(), ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(GoneRequestException.class)
    protected ResponseEntity<?> handleGoneRequestException(GoneRequestException ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.GONE,
                "410", ex.getLocalizedMessage(), ex.getMessage());
        return buildResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
        ApiError apiError = new ApiError(HttpStatus.INTERNAL_SERVER_ERROR,
                "500", ex.getLocalizedMessage(), ex.getMessage());
        return buildResponseEntity(apiError);
    }

    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }
}
