package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;

public interface MessageProcessorHandler {
  MessageResponse process(MessageContext  messageContext);
  
  
}
