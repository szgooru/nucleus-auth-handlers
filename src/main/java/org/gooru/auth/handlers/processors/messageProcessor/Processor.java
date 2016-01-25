package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.processors.command.executor.MessageResponse;

public interface Processor {
  public MessageResponse process();
}
