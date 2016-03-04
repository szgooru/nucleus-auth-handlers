package org.gooru.nucleus.auth.handlers.processors.exceptions;

public class UnauthorizedException extends RuntimeException {

  /**
   * 
   */
  private static final long serialVersionUID = -8724444379963076817L;

  public UnauthorizedException() {
  }

  public UnauthorizedException(String message) {
    super(message);
  }

  public UnauthorizedException(Throwable cause) {
    super(cause);
  }

  public UnauthorizedException(String message, Throwable cause) {
    super(message, cause);
  }

}
