package org.gooru.nucleus.auth.handlers.processors.command.executor;


public interface DBExecutor {
  void checkSanity();

  void validateRequest();

  MessageResponse executeRequest();

  boolean handlerReadOnly();
}
