package org.gooru.nucleus.auth.handlers.processors.command.executor;

import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public abstract class Executor {
  public abstract MessageResponse execute(MessageContext messageContext);

}
