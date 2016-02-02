package org.gooru.auth.handlers.processors.command.executor;

import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;

public abstract class Executor {
  public abstract MessageResponse execute(MessageContext messageContext);

}
