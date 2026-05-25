package de.hexfieldsstudio.hexfieldsdominion.error;

public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}