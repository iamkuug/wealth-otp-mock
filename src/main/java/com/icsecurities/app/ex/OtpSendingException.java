package com.icsecurities.app.ex;

public class OtpSendingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OtpSendingException(String message) {
        super(message);
    }
}
