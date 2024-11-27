package com.icsecurities.app;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiError {
    private HttpStatus status;
    private String statusCode;
    private String debugMessage;
    private String message;
}
