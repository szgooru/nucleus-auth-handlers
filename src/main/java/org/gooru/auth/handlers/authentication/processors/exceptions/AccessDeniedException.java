package org.gooru.auth.handlers.authentication.processors.exceptions;

public class AccessDeniedException extends RuntimeException {

 
  private static final long serialVersionUID = 255138652984987721L;

  public AccessDeniedException() {
  }

  public AccessDeniedException(String message, String customMsgCode) {
    super(message);
  }

  public AccessDeniedException(Throwable cause) {
    super(cause);
  }

  public AccessDeniedException(String message, Throwable cause) {
    super(message, cause);
  }
}
