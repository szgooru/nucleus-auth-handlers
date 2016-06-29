package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class ConflictException extends RuntimeException {

    private static final long serialVersionUID = 1733886064355906276L;

    public ConflictException() {
    }

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(Throwable cause) {
        super(cause);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }

}
