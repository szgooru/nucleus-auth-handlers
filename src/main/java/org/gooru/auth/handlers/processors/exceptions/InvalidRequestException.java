package org.gooru.auth.handlers.processors.exceptions;

public class InvalidRequestException extends RuntimeException {

  private static final long serialVersionUID = 6889759867639683831L;

  public InvalidRequestException() { 
    super();
  }
  
  public InvalidRequestException(String message) {
    super(message);
  }

  public InvalidRequestException(Throwable cause) {
    super(cause);
  }

  public InvalidRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}
