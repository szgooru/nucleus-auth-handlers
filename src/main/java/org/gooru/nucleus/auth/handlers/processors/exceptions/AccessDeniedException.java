package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class AccessDeniedException extends RuntimeException {

 
  private static final long serialVersionUID = 255138652984987721L;

  public AccessDeniedException() {
  }

  public AccessDeniedException(String message) {
    super(message);
  }

  public AccessDeniedException(Throwable cause) {
    super(cause);
  }

  public AccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }
}
