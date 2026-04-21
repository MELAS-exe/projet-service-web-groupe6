package com.voom.messagingservice.domain.exception;

public class DecryptionException extends RuntimeException {
    public DecryptionException(String message) {
        super(message);
    }
}
