package org.gooru.auth.handlers.processors.exceptions;

public class InvalidUserException extends RuntimeException {
  
  private static final long serialVersionUID = 159083293809696372L;

  public InvalidUserException() {
  }

  public InvalidUserException(String message) {
    super(message);
  }

  public InvalidUserException(Throwable cause) {
    super(cause);
  }

  public InvalidUserException(String message, Throwable cause) {
    super(message, cause);
  }
}
