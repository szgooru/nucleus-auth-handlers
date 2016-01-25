package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;


public abstract class Executor extends ServerValidatorUtility {
  public abstract  MessageResponse execute(MessageContext messageContext);

  
}
