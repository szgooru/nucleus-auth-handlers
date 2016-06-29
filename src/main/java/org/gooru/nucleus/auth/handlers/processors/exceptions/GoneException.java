package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class GoneException extends RuntimeException {

    private static final long serialVersionUID = 1733886064355906276L;

    public GoneException() {
    }

    public GoneException(String message) {
        super(message);
    }

    public GoneException(Throwable cause) {
        super(cause);
    }

    public GoneException(String message, Throwable cause) {
        super(message, cause);
    }

}
