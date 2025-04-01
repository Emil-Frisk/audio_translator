package com.example.application.exceptions;

public class TranscriptionException extends RuntimeException {
    public TranscriptionException(String message) {
        super(message);
    }

    public TranscriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
