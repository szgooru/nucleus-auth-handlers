package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.processors.MessageContext;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface CommandExecutor {
  MessageResponse exec(MessageContext  messageContext);
  
  
}
