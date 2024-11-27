package com.icsecurities.app.ex;

public class MessageSendingException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MessageSendingException(String message) {
        super(message);
    }
}
