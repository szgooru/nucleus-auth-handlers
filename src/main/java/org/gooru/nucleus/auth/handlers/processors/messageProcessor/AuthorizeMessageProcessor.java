package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.constants.CommandConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizeMessageProcessor implements MessageProcessorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizeMessageProcessor.class);

  @Override
  public MessageResponse process(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.AUTHORIZE:
      result = RepoFactory.getAuthorizeRepo(messageContext).authorize();
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

}
