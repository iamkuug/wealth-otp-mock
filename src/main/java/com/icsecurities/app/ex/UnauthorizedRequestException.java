package com.icsecurities.app.ex;

public class UnauthorizedRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedRequestException(String message) {
        super(message);
    }

}