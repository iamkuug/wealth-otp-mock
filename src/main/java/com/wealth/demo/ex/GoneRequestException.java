package com.wealth.demo.ex;

public class GoneRequestException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public GoneRequestException(String message) {
        super(message);
    }
}
